package com.example.ledger.application.port.in;

import java.time.LocalDate;

/** 등록한 지출의 금액·카테고리·날짜·메모 변경 유스케이스. 이미지는 바꾸지 않는다. */
public interface UpdateExpenseUseCase {

    Result update(Command command);

    record Command(long expenseId, long amount, String category, LocalDate spentAt, String memo) {
    }

    /** monthlyTotal 은 바뀐 날짜가 속한 달의 합계 */
    record Result(long expenseId, long monthlyTotal, boolean budgetExceeded) {
    }
}
