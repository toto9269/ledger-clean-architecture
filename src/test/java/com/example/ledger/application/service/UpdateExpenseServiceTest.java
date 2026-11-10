package com.example.ledger.application.service;

import com.example.ledger.application.port.in.UpdateExpenseUseCase;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.domain.Budget;
import com.example.ledger.domain.Expense;
import com.example.ledger.domain.MonthlyExpenses;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 포트를 간단한 가짜로 바꿔 끼워 변경 유스케이스만 검증한다. */
class UpdateExpenseServiceTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final List<Expense> stored = new ArrayList<>(List.of(Expense.of(1L, 10_000, "식비", LocalDate.of(2026, 10, 1), null)));
    private final List<String> alerts = new ArrayList<>();

    private final UpdateExpenseUseCase updateExpense = new UpdateExpenseService(
            expense -> stored.removeIf(e -> e.getId().equals(expense.getId())) && stored.add(expense),
            month -> new MonthlyExpenses(month, stored.stream()
                    .filter(e -> YearMonth.from(e.getSpentAt()).equals(month)).toList()),
            new BudgetPort() {
                @Override
                public void save(Budget budget) {
                }

                @Override
                public Optional<Budget> load(YearMonth month) {
                    return Optional.of(new Budget(OCTOBER, 20_000));
                }
            },
            (budget, total) -> alerts.add(budget.getMonth() + ":" + total));

    @Test
    void 금액을_올려_예산을_넘으면_알린다() {
        UpdateExpenseUseCase.Result result = updateExpense.update(
                new UpdateExpenseUseCase.Command(1L, 25_000, "식비", LocalDate.of(2026, 10, 1), null));

        assertThat(result.monthlyTotal()).isEqualTo(25_000);
        assertThat(result.budgetExceeded()).isTrue();
        assertThat(alerts).containsExactly("2026-10:25000");
    }

    @Test
    void 없는_지출은_거부한다() {
        assertThatThrownBy(() -> updateExpense.update(
                new UpdateExpenseUseCase.Command(99L, 1_000, "식비", LocalDate.of(2026, 10, 1), null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
