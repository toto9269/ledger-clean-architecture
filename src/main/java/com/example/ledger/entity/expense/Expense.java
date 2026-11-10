package com.example.ledger.entity.expense;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 지출 한 건. 만들 때와 바꿀 때 모두 "올바른 지출" 규칙을 검사한다.
 */
@Entity
@Table(name = "expense")
public class Expense {

    public static final int MEMO_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long amount;

    private String category;

    private LocalDate spentAt;

    /** 선택 입력. 비어 있으면 null */
    @Column(length = MEMO_MAX_LENGTH)
    private String memo;

    protected Expense() {
    }

    public Expense(long amount, String category, LocalDate spentAt, String memo) {
        apply(amount, category, spentAt, memo);
    }

    /** 내용을 바꾼다. 규칙을 어기면 아무것도 바꾸지 않고 예외를 던진다. */
    public void change(long amount, String category, LocalDate spentAt, String memo) {
        apply(amount, category, spentAt, memo);
    }

    private void apply(long amount, String category, LocalDate spentAt, String memo) {
        if (amount <= 0) {
            throw new IllegalArgumentException("지출 금액은 0보다 커야 합니다");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("카테고리는 비어 있을 수 없습니다");
        }
        String trimmedMemo = memo == null || memo.isBlank() ? null : memo.trim();
        if (trimmedMemo != null && trimmedMemo.length() > MEMO_MAX_LENGTH) {
            throw new IllegalArgumentException("메모는 " + MEMO_MAX_LENGTH + "자 이하여야 합니다");
        }
        this.spentAt = Objects.requireNonNull(spentAt, "지출일은 필수입니다");
        this.amount = amount;
        this.category = category.trim();
        this.memo = trimmedMemo;
    }

    public Long getId() {
        return id;
    }

    public long getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getSpentAt() {
        return spentAt;
    }

    public String getMemo() {
        return memo;
    }
}
