package com.example.ledger.service.report;

import com.example.ledger.dto.report.MonthlyReport;
import com.example.ledger.entity.budget.Budget;
import com.example.ledger.entity.expense.Expense;
import com.example.ledger.repository.budget.BudgetRepository;
import com.example.ledger.repository.expense.ExpenseImageRepository;
import com.example.ledger.repository.expense.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** 한 달 지출·예산·이미지 id 를 모아 월 리포트를 만든다. */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseImageRepository expenseImageRepository;
    private final BudgetRepository budgetRepository;

    public ReportService(ExpenseRepository expenseRepository,
                         ExpenseImageRepository expenseImageRepository,
                         BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseImageRepository = expenseImageRepository;
        this.budgetRepository = budgetRepository;
    }

    public MonthlyReport get(YearMonth month) {
        List<Expense> expenses = expenseRepository.findBySpentAtBetweenOrderBySpentAtAscIdAsc(
                month.atDay(1), month.atEndOfMonth());

        long total = expenses.stream().mapToLong(Expense::getAmount).sum();
        Map<String, Long> totalByCategory = new TreeMap<>();
        expenses.forEach(e -> totalByCategory.merge(e.getCategory(), e.getAmount(), Long::sum));
        Optional<Budget> budget = budgetRepository.findByMonth(month);
        Map<Long, List<Long>> imageIds = loadImageIds(expenses);

        List<MonthlyReport.Item> items = expenses.stream()
                .map(e -> new MonthlyReport.Item(e.getId(), e.getAmount(), e.getCategory(), e.getSpentAt(),
                        e.getMemo(), imageIds.getOrDefault(e.getId(), List.of())))
                .toList();

        return new MonthlyReport(
                month,
                total,
                budget.map(Budget::getLimit).orElse(null),
                budget.map(b -> b.remaining(total)).orElse(null),
                totalByCategory,
                items);
    }

    /** 지출 id 별 이미지 id 목록(올린 순서). 이미지 내용은 읽지 않는다. */
    private Map<Long, List<Long>> loadImageIds(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            return Map.of();
        }
        return expenseImageRepository.findByExpenseIdInOrderByIdAsc(expenses.stream().map(Expense::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(ExpenseImageRepository.ImageRef::getExpenseId,
                        Collectors.mapping(ExpenseImageRepository.ImageRef::getId, Collectors.toList())));
    }
}
