package com.example.ledger.dto.expense;

import java.time.LocalDate;
import java.util.List;

/** 지출 등록에 필요한 값. images 는 없으면 빈 목록 */
public record AddExpenseCommand(long amount, String category, LocalDate spentAt, String memo, List<ImageFile> images) {
}
