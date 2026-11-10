package com.example.ledger.entity.expense;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 지출·이미지 엔티티의 규칙은 DB 없이 바로 테스트된다. */
class ExpenseEntityTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @Test
    void 지출_금액은_0보다_커야_한다() {
        assertThatThrownBy(() -> new Expense(0, "식비", OCT_15, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 카테고리는_비어_있을_수_없다() {
        assertThatThrownBy(() -> new Expense(1000, " ", OCT_15, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메모는_100자를_넘을_수_없다() {
        assertThatThrownBy(() -> new Expense(1000, "식비", OCT_15, "가".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 빈_메모는_없는_것으로_본다() {
        assertThat(new Expense(1000, "식비", OCT_15, "  ").getMemo()).isNull();
        assertThat(new Expense(1000, "식비", OCT_15, " 점심 ").getMemo()).isEqualTo("점심");
    }

    @Test
    void 규칙을_어기는_변경은_아무것도_바꾸지_않는다() {
        Expense expense = new Expense(1000, "식비", OCT_15, "점심");

        assertThatThrownBy(() -> expense.change(0, "교통비", OCT_15, "택시"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(expense.getAmount()).isEqualTo(1000);
        assertThat(expense.getCategory()).isEqualTo("식비");
        assertThat(expense.getMemo()).isEqualTo("점심");
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
}
