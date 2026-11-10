package com.example.ledger.dto.expense;

import java.time.LocalDate;

/** POST /api/expenses (JSON) 요청 본문. spentAt 을 비우면 오늘, memo 는 선택 */
public record AddExpenseRequest(Long amount, String category, LocalDate spentAt, String memo) {
}
