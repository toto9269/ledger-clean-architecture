package com.example.ledger.application.port.out;

import com.example.ledger.domain.Budget;

/** 예산 초과 알림 — 로그·메일·알림톡 중 무엇으로 보낼지는 adapter 가 정한다. */
public interface BudgetAlertPort {

    void alertExceeded(Budget budget, long monthlyTotal);
}
