package com.example.ledger.service.budget;

import com.example.ledger.entity.budget.Budget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 예산 초과 알림. 지금은 경고 로그로 남긴다. */
@Service
public class BudgetAlertService {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertService.class);

    public void alertExceeded(Budget budget, long monthlyTotal) {
        log.warn("[예산 초과] {} 예산 {}원, 현재 지출 {}원 ({}원 초과)",
                budget.getMonth(), budget.getLimit(), monthlyTotal, -budget.remaining(monthlyTotal));
    }
}
