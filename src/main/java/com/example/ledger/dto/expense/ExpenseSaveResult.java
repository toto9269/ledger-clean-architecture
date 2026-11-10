package com.example.ledger.dto.expense;

/** 지출 등록·변경 결과. monthlyTotal 은 그 지출 날짜가 속한 달의 합계 */
public record ExpenseSaveResult(long expenseId, long monthlyTotal, boolean budgetExceeded) {
}
