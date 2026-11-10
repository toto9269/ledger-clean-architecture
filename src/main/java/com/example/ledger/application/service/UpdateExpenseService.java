package com.example.ledger.application.service;

import com.example.ledger.application.port.in.UpdateExpenseUseCase;
import com.example.ledger.application.port.out.BudgetAlertPort;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.application.port.out.LoadExpensesPort;
import com.example.ledger.application.port.out.UpdateExpensePort;
import com.example.ledger.domain.Budget;
import com.example.ledger.domain.Expense;

import java.time.YearMonth;
import java.util.Optional;

/**
 * 지출을 바꾸고, 바뀐 날짜가 속한 달의 합계가 예산을 넘으면 등록할 때처럼 알린다.
 */
public class UpdateExpenseService implements UpdateExpenseUseCase {

    private final UpdateExpensePort updateExpensePort;
    private final LoadExpensesPort loadExpensesPort;
    private final BudgetPort budgetPort;
    private final BudgetAlertPort budgetAlertPort;

    public UpdateExpenseService(UpdateExpensePort updateExpensePort,
                                LoadExpensesPort loadExpensesPort,
                                BudgetPort budgetPort,
                                BudgetAlertPort budgetAlertPort) {
        this.updateExpensePort = updateExpensePort;
        this.loadExpensesPort = loadExpensesPort;
        this.budgetPort = budgetPort;
        this.budgetAlertPort = budgetAlertPort;
    }

    @Override
    public Result update(Command command) {
        // 도메인 객체를 새로 만들며 금액·카테고리·메모 규칙을 다시 검사한다
        Expense expense = Expense.of(command.expenseId(), command.amount(), command.category(),
                command.spentAt(), command.memo());
        if (!updateExpensePort.update(expense)) {
            throw new IllegalArgumentException("이미 삭제됐거나 없는 지출입니다");
        }

        YearMonth month = YearMonth.from(expense.getSpentAt());
        long monthlyTotal = loadExpensesPort.loadMonthly(month).total();
        Optional<Budget> budget = budgetPort.load(month);
        boolean exceeded = budget.map(b -> b.isExceededBy(monthlyTotal)).orElse(false);
        if (exceeded) {
            budgetAlertPort.alertExceeded(budget.get(), monthlyTotal);
        }
        return new Result(expense.getId(), monthlyTotal, exceeded);
    }
}
