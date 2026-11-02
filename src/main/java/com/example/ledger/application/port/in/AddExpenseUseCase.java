package com.example.ledger.application.port.in;

import java.time.LocalDate;

/** 지출 등록 유스케이스 — 웹 같은 바깥 계층은 이 약속만 보고 호출한다. */
public interface AddExpenseUseCase {

    Result add(Command command);

    record Command(long amount, String category, LocalDate spentAt) {
    }

    record Result(long expenseId, long monthlyTotal, boolean budgetExceeded) {
    }
}
