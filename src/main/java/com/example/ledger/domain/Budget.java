package com.example.ledger.domain;

import java.time.YearMonth;
import java.util.Objects;

/**
 * 한 달 예산. 예산 초과 여부와 남은 금액 계산은 이 객체의 책임이다.
 */
public class Budget {

    private final YearMonth month;
    private final long limit;

    public Budget(YearMonth month, long limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("예산은 0보다 커야 합니다");
        }
        this.month = Objects.requireNonNull(month, "예산 월은 필수입니다");
        this.limit = limit;
    }

    public boolean isExceededBy(long monthlyTotal) {
        return monthlyTotal > limit;
    }

    public long remaining(long monthlyTotal) {
        return limit - monthlyTotal;
    }

    public YearMonth getMonth() {
        return month;
    }

    public long getLimit() {
        return limit;
    }
}
