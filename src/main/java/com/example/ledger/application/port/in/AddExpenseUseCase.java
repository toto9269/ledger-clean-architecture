package com.example.ledger.application.port.in;

import java.time.LocalDate;
import java.util.List;

/** 지출 등록 유스케이스 — 웹 같은 바깥 계층은 이 약속만 보고 호출한다. */
public interface AddExpenseUseCase {

    Result add(Command command);

    /** images 는 없으면 빈 목록 */
    record Command(long amount, String category, LocalDate spentAt, String memo, List<ImageFile> images) {
    }

    record ImageFile(String contentType, byte[] data) {
    }

    record Result(long expenseId, long monthlyTotal, boolean budgetExceeded) {
    }
}
