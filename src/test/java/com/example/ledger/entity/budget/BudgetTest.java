package com.example.ledger.entity.budget;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BudgetTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    @Test
    void 예산은_0보다_커야_한다() {
        assertThatThrownBy(() -> new Budget(OCTOBER, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예산을_넘으면_초과다() {
        Budget budget = new Budget(OCTOBER, 100_000);

        assertThat(budget.isExceededBy(100_000)).isFalse();
        assertThat(budget.isExceededBy(100_001)).isTrue();
        assertThat(budget.remaining(80_000)).isEqualTo(20_000);
        assertThat(budget.getMonth()).isEqualTo(OCTOBER);
    }
}
