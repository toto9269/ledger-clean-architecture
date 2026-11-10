package com.example.ledger.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 도메인 규칙은 Spring·DB 없이 바로 테스트된다. */
class DomainTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    @Test
    void 지출_금액은_0보다_커야_한다() {
        assertThatThrownBy(() -> Expense.create(0, "식비", OCT_15))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 카테고리는_비어_있을_수_없다() {
        assertThatThrownBy(() -> Expense.create(1000, " ", OCT_15))
                .isInstanceOf(IllegalArgumentException.class);
    }

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
    }

    @Test
    void 한_달_합계와_카테고리별_합계를_계산한다() {
        MonthlyExpenses monthly = new MonthlyExpenses(OCTOBER, List.of(
                Expense.of(1L, 12_000, "식비", OCT_15),
                Expense.of(2L, 3_000, "교통", OCT_15),
                Expense.of(3L, 8_000, "식비", OCT_15)));

        assertThat(monthly.total()).isEqualTo(23_000);
        assertThat(monthly.totalByCategory()).isEqualTo(Map.of("식비", 20_000L, "교통", 3_000L));
    }

    @Test
    void 다른_달의_지출은_섞일_수_없다() {
        assertThatThrownBy(() -> new MonthlyExpenses(OCTOBER,
                List.of(Expense.of(1L, 1000, "식비", LocalDate.of(2026, 11, 1)))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
