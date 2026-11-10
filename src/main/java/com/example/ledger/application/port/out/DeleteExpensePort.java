package com.example.ledger.application.port.out;

import java.util.Collection;

/** 지출 삭제 */
public interface DeleteExpensePort {

    /** 실제로 지운 건수를 돌려준다. */
    int deleteAll(Collection<Long> expenseIds);
}
