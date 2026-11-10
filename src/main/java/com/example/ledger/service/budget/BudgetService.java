package com.example.ledger.service.budget;

import com.example.ledger.entity.budget.Budget;
import com.example.ledger.repository.budget.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /** 그 달 예산을 새로 정하거나 바꾼다. */
    public void set(YearMonth month, long limit) {
        budgetRepository.save(new Budget(month, limit));
    }
}
