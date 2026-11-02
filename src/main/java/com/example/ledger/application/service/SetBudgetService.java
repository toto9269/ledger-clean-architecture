package com.example.ledger.application.service;

import com.example.ledger.application.port.in.SetBudgetUseCase;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.domain.Budget;

import java.time.YearMonth;

public class SetBudgetService implements SetBudgetUseCase {

    private final BudgetPort budgetPort;

    public SetBudgetService(BudgetPort budgetPort) {
        this.budgetPort = budgetPort;
    }

    @Override
    public void set(YearMonth month, long limit) {
        budgetPort.save(new Budget(month, limit));
    }
}
