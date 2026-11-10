package com.example.ledger.application.port.out;

import com.example.ledger.domain.Expense;

/** 지출 저장 — 구현(JPA 등)은 adapter 가 맡는다. */
public interface SaveExpensePort {

    /** 저장 후 id 가 채워진 지출을 돌려준다. */
    Expense save(Expense expense);
}
