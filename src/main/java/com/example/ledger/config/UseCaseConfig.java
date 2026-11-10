package com.example.ledger.config;

import com.example.ledger.application.port.in.AddExpenseUseCase;
import com.example.ledger.application.port.in.DeleteExpensesUseCase;
import com.example.ledger.application.port.in.GetExpenseImageUseCase;
import com.example.ledger.application.port.in.GetMonthlyReportUseCase;
import com.example.ledger.application.port.in.SetBudgetUseCase;
import com.example.ledger.application.port.out.BudgetAlertPort;
import com.example.ledger.application.port.out.BudgetPort;
import com.example.ledger.application.port.out.DeleteExpensePort;
import com.example.ledger.application.port.out.ExpenseImagePort;
import com.example.ledger.application.port.out.LoadExpensesPort;
import com.example.ledger.application.port.out.SaveExpensePort;
import com.example.ledger.application.service.AddExpenseService;
import com.example.ledger.application.service.DeleteExpensesService;
import com.example.ledger.application.service.GetExpenseImageService;
import com.example.ledger.application.service.GetMonthlyReportService;
import com.example.ledger.application.service.SetBudgetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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

    /**
     * 이미지 삭제와 지출 삭제를 하나의 트랜잭션으로 묶는다. 중간에 실패하면 모두 되돌린다.
     * 유스케이스는 Spring 을 모르므로 트랜잭션은 이 조립 지점에서 감싼다.
     */
    @Bean
    DeleteExpensesUseCase deleteExpensesUseCase(DeleteExpensePort deleteExpensePort,
                                                ExpenseImagePort expenseImagePort,
                                                PlatformTransactionManager transactionManager) {
        DeleteExpensesUseCase service = new DeleteExpensesService(deleteExpensePort, expenseImagePort);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return expenseIds -> transaction.execute(status -> service.delete(expenseIds));
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
