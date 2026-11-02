package com.example.ledger.domain;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 한 달 동안의 지출 묶음. 합계와 카테고리별 합계를 계산한다.
 */
public class MonthlyExpenses {

    private final YearMonth month;
    private final List<Expense> expenses;

    public MonthlyExpenses(YearMonth month, List<Expense> expenses) {
        boolean outOfMonth = expenses.stream()
                .anyMatch(expense -> !YearMonth.from(expense.getSpentAt()).equals(month));
        if (outOfMonth) {
            throw new IllegalArgumentException(month + " 이외의 지출이 섞여 있습니다");
        }
        this.month = month;
        this.expenses = List.copyOf(expenses);
    }

    public long total() {
        return expenses.stream().mapToLong(Expense::getAmount).sum();
    }

    public Map<String, Long> totalByCategory() {
        Map<String, Long> totals = new TreeMap<>();
        expenses.forEach(expense -> totals.merge(expense.getCategory(), expense.getAmount(), Long::sum));
        return totals;
    }

    public YearMonth getMonth() {
        return month;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }
}
