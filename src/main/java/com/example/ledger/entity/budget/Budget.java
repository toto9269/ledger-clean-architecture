package com.example.ledger.entity.budget;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.YearMonth;
import java.util.Objects;

/**
 * 월 예산. 기본 키는 "2026-10" 같은 연월 문자열이다.
 * 예산 규칙(0보다 커야 함)과 초과 여부·남은 금액 계산도 이 클래스가 맡는다.
 */
@Entity
@Table(name = "budget")
public class Budget {

    @Id
    private String yearMonth;

    private long amountLimit;

    protected Budget() {
    }

    public Budget(YearMonth month, long limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("예산은 0보다 커야 합니다");
        }
        this.yearMonth = Objects.requireNonNull(month, "예산 월은 필수입니다").toString();
        this.amountLimit = limit;
    }

    public boolean isExceededBy(long monthlyTotal) {
        return monthlyTotal > amountLimit;
    }

    public long remaining(long monthlyTotal) {
        return amountLimit - monthlyTotal;
    }

    public YearMonth getMonth() {
        return YearMonth.parse(yearMonth);
    }

    public long getLimit() {
        return amountLimit;
    }
}
