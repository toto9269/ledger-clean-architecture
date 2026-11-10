package com.example.ledger.config;

import com.example.ledger.application.port.in.AddExpenseUseCase;
import com.example.ledger.application.port.in.GetExpenseImageUseCase;
import com.example.ledger.application.port.in.GetMonthlyReportUseCase;
import com.example.ledger.application.port.in.SetBudgetUseCase;
import com.example.ledger.application.port.out.BudgetAlertPort;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.application.port.out.ExpenseImagePort;
import com.example.ledger.application.port.out.LoadExpensesPort;
import com.example.ledger.application.port.out.SaveExpensePort;
import com.example.ledger.application.service.AddExpenseService;
import com.example.ledger.application.service.GetExpenseImageService;
import com.example.ledger.application.service.GetMonthlyReportService;
import com.example.ledger.application.service.SetBudgetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 조립 지점. 유스케이스는 Spring 을 모르므로(@Service 없음) 여기서 포트 구현체를 넣어 빈으로 만든다.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    AddExpenseUseCase addExpenseUseCase(SaveExpensePort saveExpensePort,
                                        LoadExpensesPort loadExpensesPort,
                                        BudgetPort budgetPort,
                                        BudgetAlertPort budgetAlertPort,
                                        ExpenseImagePort expenseImagePort) {
        return new AddExpenseService(saveExpensePort, loadExpensesPort, budgetPort, budgetAlertPort, expenseImagePort);
    }

    @Bean
    GetExpenseImageUseCase getExpenseImageUseCase(ExpenseImagePort expenseImagePort) {
        return new GetExpenseImageService(expenseImagePort);
    }

    @Bean
    SetBudgetUseCase setBudgetUseCase(BudgetPort budgetPort) {
        return new SetBudgetService(budgetPort);
    }

    @Bean
    GetMonthlyReportUseCase getMonthlyReportUseCase(LoadExpensesPort loadExpensesPort, BudgetPort budgetPort,
                                                    ExpenseImagePort expenseImagePort) {
        return new GetMonthlyReportService(loadExpensesPort, budgetPort, expenseImagePort);
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
