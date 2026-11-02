package com.example.ledger.application.service;

import com.example.ledger.application.port.in.AddExpenseUseCase;
import com.example.ledger.application.port.out.BudgetAlertPort;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.application.port.out.LoadExpensesPort;
import com.example.ledger.application.port.out.SaveExpensePort;
import com.example.ledger.domain.Budget;
import com.example.ledger.domain.Expense;
import com.example.ledger.domain.MonthlyExpenses;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** 포트를 메모리 가짜로 바꿔 끼워, DB·Spring·알림 없이 유스케이스를 검증한다. */
class AddExpenseServiceTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final InMemoryLedger ledger = new InMemoryLedger();
    private final List<String> alerts = new ArrayList<>();
    private final BudgetAlertPort alertPort = (budget, total) -> alerts.add(budget.getMonth() + ":" + total);
    private final AddExpenseUseCase addExpense = new AddExpenseService(ledger, ledger, ledger, alertPort);

    @Test
    void 예산_안이면_알리지_않는다() {
        ledger.save(new Budget(OCTOBER, 50_000));

        AddExpenseUseCase.Result result = addExpense.add(command(30_000));

        assertThat(result.monthlyTotal()).isEqualTo(30_000);
        assertThat(result.budgetExceeded()).isFalse();
        assertThat(alerts).isEmpty();
    }

    @Test
    void 예산을_넘는_순간_알린다() {
        ledger.save(new Budget(OCTOBER, 50_000));
        addExpense.add(command(30_000));

        AddExpenseUseCase.Result result = addExpense.add(command(25_000));

        assertThat(result.budgetExceeded()).isTrue();
        assertThat(alerts).containsExactly("2026-10:55000");
    }

    @Test
    void 예산이_없는_달은_알리지_않는다() {
        AddExpenseUseCase.Result result = addExpense.add(command(1_000_000));

        assertThat(result.budgetExceeded()).isFalse();
        assertThat(alerts).isEmpty();
    }

    private static AddExpenseUseCase.Command command(long amount) {
        return new AddExpenseUseCase.Command(amount, "식비", LocalDate.of(2026, 10, 15));
    }

    /** 세 개의 출력 포트를 메모리로 구현한 테스트용 저장소 */
    private static class InMemoryLedger implements SaveExpensePort, LoadExpensesPort, BudgetPort {

        private final List<Expense> expenses = new ArrayList<>();
        private final Map<YearMonth, Budget> budgets = new HashMap<>();

        @Override
        public Expense save(Expense expense) {
            Expense saved = Expense.of((long) expenses.size() + 1,
                    expense.getAmount(), expense.getCategory(), expense.getSpentAt());
            expenses.add(saved);
            return saved;
        }

        @Override
        public MonthlyExpenses loadMonthly(YearMonth month) {
            return new MonthlyExpenses(month, expenses.stream()
                    .filter(e -> YearMonth.from(e.getSpentAt()).equals(month))
                    .toList());
        }

        @Override
        public void save(Budget budget) {
            budgets.put(budget.getMonth(), budget);
        }

        @Override
        public Optional<Budget> load(YearMonth month) {
            return Optional.ofNullable(budgets.get(month));
        }
    }
}
