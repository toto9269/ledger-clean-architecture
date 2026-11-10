package com.example.ledger.dto.budget;

/** PUT /api/budgets/{yyyy-MM} 요청 본문 */
public record SetBudgetRequest(Long limit) {
}
