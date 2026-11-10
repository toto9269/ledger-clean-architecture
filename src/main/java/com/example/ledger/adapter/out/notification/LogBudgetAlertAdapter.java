package com.example.ledger.adapter.out.notification;

import com.example.ledger.application.port.out.BudgetAlertPort;
import com.example.ledger.domain.Budget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 지금은 로그로 알린다. 메일·알림톡으로 바꾸려면 BudgetAlertPort 구현체만 새로 만들면 된다. */
@Component
class LogBudgetAlertAdapter implements BudgetAlertPort {

    private static final Logger log = LoggerFactory.getLogger(LogBudgetAlertAdapter.class);

    @Override
    public void alertExceeded(Budget budget, long monthlyTotal) {
        log.warn("[예산 초과] {} 예산 {}원, 현재 지출 {}원 ({}원 초과)",
                budget.getMonth(), budget.getLimit(), monthlyTotal, -budget.remaining(monthlyTotal));
    }
}
