package com.example.ledger.adapter.out.persistence;

import com.example.ledger.application.port.out.LoadExpensesPort;
import com.example.ledger.application.port.out.SaveExpensePort;
import com.example.ledger.domain.Expense;
import com.example.ledger.domain.MonthlyExpenses;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

/** 지출 포트를 JPA 로 구현한다. DB 를 바꾸면 이 클래스만 새로 만들면 된다. */
@Component
class ExpensePersistenceAdapter implements SaveExpensePort, LoadExpensesPort {

    private final ExpenseJpaRepository repository;

    ExpensePersistenceAdapter(ExpenseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Expense save(Expense expense) {
        ExpenseJpaEntity saved = repository.save(
                new ExpenseJpaEntity(expense.getAmount(), expense.getCategory(), expense.getSpentAt(), expense.getMemo()));
        return toDomain(saved);
    }

    @Override
    public MonthlyExpenses loadMonthly(YearMonth month) {
        return new MonthlyExpenses(month, repository
                .findBySpentAtBetweenOrderBySpentAtAscIdAsc(month.atDay(1), month.atEndOfMonth())
                .stream()
                .map(ExpensePersistenceAdapter::toDomain)
                .toList());
    }

    private static Expense toDomain(ExpenseJpaEntity entity) {
        return Expense.of(entity.getId(), entity.getAmount(), entity.getCategory(), entity.getSpentAt(), entity.getMemo());
    }
}
