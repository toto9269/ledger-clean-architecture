package com.example.ledger.application.port.out;

import com.example.ledger.domain.MonthlyExpenses;

import java.time.YearMonth;

/** 한 달 지출 조회 */
public interface LoadExpensesPort {

    MonthlyExpenses loadMonthly(YearMonth month);
}
