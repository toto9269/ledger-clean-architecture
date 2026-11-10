package com.example.ledger.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 월 예산 테이블 — 기본 키는 "2026-10" 같은 연월 문자열 */
@Entity
@Table(name = "budget")
class BudgetJpaEntity {

    @Id
    private String yearMonth;

    private long amountLimit;

    protected BudgetJpaEntity() {
    }

    BudgetJpaEntity(String yearMonth, long amountLimit) {
        this.yearMonth = yearMonth;
        this.amountLimit = amountLimit;
    }

    String getYearMonth() {
        return yearMonth;
    }

    long getAmountLimit() {
        return amountLimit;
    }
}
