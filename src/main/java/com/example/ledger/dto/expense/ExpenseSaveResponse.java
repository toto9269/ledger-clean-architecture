package com.example.ledger.dto.expense;

import com.example.ledger.dto.expense.ExpenseSaveResult;

/** 지출 등록·변경 응답 */
public record ExpenseSaveResponse(long expenseId, long monthlyTotal, boolean budgetExceeded) {

    public static ExpenseSaveResponse from(ExpenseSaveResult result) {
        return new ExpenseSaveResponse(result.expenseId(), result.monthlyTotal(), result.budgetExceeded());
    }
}
