package com.example.ledger.dto.expense;

import java.time.LocalDate;

/** PUT /api/expenses/{id} 요청 본문. amount 와 spentAt 은 필수, memo 는 선택 */
public record UpdateExpenseRequest(Long amount, String category, LocalDate spentAt, String memo) {
}
