package com.example.ledger.application.service;

import com.example.ledger.application.port.in.AddExpenseUseCase;
import com.example.ledger.application.port.out.BudgetAlertPort;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.application.port.out.LoadExpensesPort;
import com.example.ledger.application.port.out.SaveExpensePort;
import com.example.ledger.domain.Budget;
import com.example.ledger.domain.Expense;

import java.time.YearMonth;
import java.util.Optional;

/**
 * 지출을 저장하고, 그 달 합계가 예산을 넘으면 알린다.
 * 포트(인터페이스)만 알고 JPA·Spring·알림 수단은 모른다.
 */
public class AddExpenseService implements AddExpenseUseCase {

    private final SaveExpensePort saveExpensePort;
    private final LoadExpensesPort loadExpensesPort;
    private final BudgetPort budgetPort;
    private final BudgetAlertPort budgetAlertPort;

    public AddExpenseService(SaveExpensePort saveExpensePort,
                             LoadExpensesPort loadExpensesPort,
                             BudgetPort budgetPort,
                             BudgetAlertPort budgetAlertPort) {
        this.saveExpensePort = saveExpensePort;
        this.loadExpensesPort = loadExpensesPort;
        this.budgetPort = budgetPort;
        this.budgetAlertPort = budgetAlertPort;
    }

    @Override
    public Result add(Command command) {
        Expense saved = saveExpensePort.save(
                Expense.create(command.amount(), command.category(), command.spentAt()));

        YearMonth month = YearMonth.from(saved.getSpentAt());
        long monthlyTotal = loadExpensesPort.loadMonthly(month).total();

        Optional<Budget> budget = budgetPort.load(month);
        boolean exceeded = budget.map(b -> b.isExceededBy(monthlyTotal)).orElse(false);
        if (exceeded) {
            budgetAlertPort.alertExceeded(budget.get(), monthlyTotal);
        }

        return new Result(saved.getId(), monthlyTotal, exceeded);
    }
}
