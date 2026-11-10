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
        assertThatThrownBy(() -> Expense.create(0, "식비", OCT_15, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 카테고리는_비어_있을_수_없다() {
        assertThatThrownBy(() -> Expense.create(1000, " ", OCT_15, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메모는_100자를_넘을_수_없다() {
        assertThatThrownBy(() -> Expense.create(1000, "식비", OCT_15, "가".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 빈_메모는_없는_것으로_본다() {
        assertThat(Expense.create(1000, "식비", OCT_15, "  ").getMemo()).isNull();
        assertThat(Expense.create(1000, "식비", OCT_15, " 점심 ").getMemo()).isEqualTo("점심");
    }

    @Test
    void 이미지는_JPG_PNG_GIF_WEBP만_허용한다() {
        assertThatThrownBy(() -> new ExpenseImage("application/pdf", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new ExpenseImage("image/png", new byte[]{1}).getContentType()).isEqualTo("image/png");
    }

    @Test
    void 이미지는_5MB를_넘을_수_없다() {
        assertThatThrownBy(() -> new ExpenseImage("image/jpeg", new byte[ExpenseImage.MAX_BYTES + 1]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미지는_지출당_5장까지다() {
        ExpenseImage.checkCount(5);
        assertThatThrownBy(() -> ExpenseImage.checkCount(6))
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
                Expense.of(1L, 12_000, "식비", OCT_15, null),
                Expense.of(2L, 3_000, "교통", OCT_15, null),
                Expense.of(3L, 8_000, "식비", OCT_15, null)));

        assertThat(monthly.total()).isEqualTo(23_000);
        assertThat(monthly.totalByCategory()).isEqualTo(Map.of("식비", 20_000L, "교통", 3_000L));
    }

    @Test
    void 다른_달의_지출은_섞일_수_없다() {
        assertThatThrownBy(() -> new MonthlyExpenses(OCTOBER,
                List.of(Expense.of(1L, 1000, "식비", LocalDate.of(2026, 11, 1), null))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
