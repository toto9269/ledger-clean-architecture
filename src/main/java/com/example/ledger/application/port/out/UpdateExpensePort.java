package com.example.ledger.application.port.out;

import com.example.ledger.domain.Expense;

/** 이미 저장된 지출을 바꾼다. */
public interface UpdateExpensePort {

    /** 바꾼 내용을 저장한다. 없는 지출이면 false 를 돌려준다. */
    boolean update(Expense expense);
}
