package com.example.ledger.service.expense;

import com.example.ledger.dto.expense.AddExpenseCommand;
import com.example.ledger.dto.expense.ExpenseSaveResult;
import com.example.ledger.dto.expense.ImageFile;
import com.example.ledger.dto.expense.UpdateExpenseCommand;
import com.example.ledger.entity.budget.Budget;
import com.example.ledger.entity.expense.Expense;
import com.example.ledger.entity.expense.ExpenseImage;
import com.example.ledger.repository.budget.BudgetRepository;
import com.example.ledger.repository.expense.ExpenseImageRepository;
import com.example.ledger.repository.expense.ExpenseRepository;
import com.example.ledger.service.budget.BudgetAlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 지출 등록·변경·삭제와 이미지 조회. 메서드마다 하나의 트랜잭션으로 실행되어,
 * 중간에 예외가 나면 그 메서드에서 한 저장·삭제가 모두 되돌려진다.
 */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseImageRepository expenseImageRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetAlertService budgetAlertService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseImageRepository expenseImageRepository,
                          BudgetRepository budgetRepository,
                          BudgetAlertService budgetAlertService) {
        this.expenseRepository = expenseRepository;
        this.expenseImageRepository = expenseImageRepository;
        this.budgetRepository = budgetRepository;
        this.budgetAlertService = budgetAlertService;
    }

    /** 지출과 이미지를 저장하고, 그 달 합계가 예산을 넘으면 알린다. */
    public ExpenseSaveResult add(AddExpenseCommand command) {
        // 지출과 이미지를 모두 검사한 뒤에 저장한다
        Expense expense = new Expense(command.amount(), command.category(), command.spentAt(), command.memo());
        ExpenseImage.checkCount(command.images().size());
        List<ExpenseImage> images = command.images().stream()
                .map(file -> new ExpenseImage(file.contentType(), file.data()))
                .toList();

        expenseRepository.save(expense);
        images.forEach(image -> image.attachTo(expense.getId()));
        expenseImageRepository.saveAll(images);
        return checkBudget(expense);
    }

    /** 금액·카테고리·날짜·메모를 바꾼다. 이미지는 그대로 둔다. */
    public ExpenseSaveResult update(UpdateExpenseCommand command) {
        Expense expense = expenseRepository.findById(command.expenseId())
                .orElseThrow(() -> new IllegalArgumentException("이미 삭제됐거나 없는 지출입니다"));
        expense.change(command.amount(), command.category(), command.spentAt(), command.memo());
        return checkBudget(expense);
    }

    /** 여러 건을 지운다. 딸린 이미지도 함께 지우고, 없는 지출이 섞이면 아무것도 지우지 않는다. */
    public int delete(List<Long> expenseIds) {
        List<Long> ids = expenseIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("삭제할 지출을 선택하세요");
        }

        // 딸린 이미지를 먼저 지워, 지출 없이 이미지만 남는 행이 생기지 않게 한다
        expenseImageRepository.deleteByExpenseIdIn(ids);
        int deleted = expenseRepository.deleteByIdIn(ids);
        if (deleted != ids.size()) {
            // 여기서 던지면 트랜잭션이 위의 삭제를 모두 되돌린다
            throw new IllegalArgumentException("이미 삭제됐거나 없는 지출이 포함되어 있습니다");
        }
        return deleted;
    }

    @Transactional(readOnly = true)
    public Optional<ImageFile> getImage(long imageId) {
        return expenseImageRepository.findById(imageId)
                .map(image -> new ImageFile(image.getContentType(), image.getData()));
    }

    /** 지출 날짜가 속한 달의 합계를 구하고, 예산을 넘었으면 알린다. */
    private ExpenseSaveResult checkBudget(Expense expense) {
        YearMonth month = YearMonth.from(expense.getSpentAt());
        long monthlyTotal = expenseRepository.sumAmountBetween(month.atDay(1), month.atEndOfMonth());
        Optional<Budget> budget = budgetRepository.findByMonth(month);
        boolean exceeded = budget.map(b -> b.isExceededBy(monthlyTotal)).orElse(false);
        if (exceeded) {
            budgetAlertService.alertExceeded(budget.get(), monthlyTotal);
        }
        return new ExpenseSaveResult(expense.getId(), monthlyTotal, exceeded);
    }
}
