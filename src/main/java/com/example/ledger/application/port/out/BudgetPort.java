package com.example.ledger.application.port.out;

import com.example.ledger.domain.Budget;

import java.time.YearMonth;
import java.util.Optional;

/** 월 예산 저장·조회 */
public interface BudgetPort {

    void save(Budget budget);

    Optional<Budget> load(YearMonth month);
}
