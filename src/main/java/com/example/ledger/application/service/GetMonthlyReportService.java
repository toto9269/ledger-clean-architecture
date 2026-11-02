package com.example.ledger.application.service;

import com.example.ledger.application.port.in.GetMonthlyReportUseCase;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.application.port.out.LoadExpensesPort;
import com.example.ledger.domain.Budget;
import com.example.ledger.domain.MonthlyExpenses;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class GetMonthlyReportService implements GetMonthlyReportUseCase {

    private final LoadExpensesPort loadExpensesPort;
    private final BudgetPort budgetPort;

    public GetMonthlyReportService(LoadExpensesPort loadExpensesPort, BudgetPort budgetPort) {
        this.loadExpensesPort = loadExpensesPort;
        this.budgetPort = budgetPort;
    }

    @Override
    public Report get(YearMonth month) {
        MonthlyExpenses monthly = loadExpensesPort.loadMonthly(month);
        long total = monthly.total();
        Optional<Budget> budget = budgetPort.load(month);

        List<Item> items = monthly.getExpenses().stream()
                .map(e -> new Item(e.getId(), e.getAmount(), e.getCategory(), e.getSpentAt()))
                .toList();

        return new Report(
                month,
                total,
                budget.map(Budget::getLimit).orElse(null),
                budget.map(b -> b.remaining(total)).orElse(null),
                monthly.totalByCategory(),
                items);
    }
}
