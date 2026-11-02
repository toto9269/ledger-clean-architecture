package com.example.ledger.application.port.in;

import java.time.YearMonth;

/** 월 예산 설정 유스케이스 */
public interface SetBudgetUseCase {

    void set(YearMonth month, long limit);
}
