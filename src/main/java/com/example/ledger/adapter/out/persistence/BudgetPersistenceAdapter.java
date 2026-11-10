package com.example.ledger.adapter.out.persistence;

import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.domain.Budget;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Optional;

@Component
class BudgetPersistenceAdapter implements BudgetPort {

    private final BudgetJpaRepository repository;

    BudgetPersistenceAdapter(BudgetJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Budget budget) {
        repository.save(new BudgetJpaEntity(budget.getMonth().toString(), budget.getLimit()));
    }

    @Override
    public Optional<Budget> load(YearMonth month) {
        return repository.findById(month.toString())
                .map(entity -> new Budget(YearMonth.parse(entity.getYearMonth()), entity.getAmountLimit()));
    }
}
