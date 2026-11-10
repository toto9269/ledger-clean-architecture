package com.example.ledger.dto.expense;

import java.time.LocalDate;

/** 지출 변경에 필요한 값. 이미지는 바꾸지 않는다. */
public record UpdateExpenseCommand(long expenseId, long amount, String category, LocalDate spentAt, String memo) {
}
