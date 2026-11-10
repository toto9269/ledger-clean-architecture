package com.example.ledger.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 지출 한 건. 프레임워크를 모르는 순수 Java 객체이고, "올바른 지출"의 규칙을 스스로 지킨다.
 */
public class Expense {

    private final Long id;
    private final long amount;
    private final String category;
    private final LocalDate spentAt;
    /** 선택 입력. 비어 있으면 null */
    private final String memo;

    public static final int MEMO_MAX_LENGTH = 100;

    private Expense(Long id, long amount, String category, LocalDate spentAt, String memo) {
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
        this.id = id;
        this.amount = amount;
        this.category = category.trim();
        this.spentAt = Objects.requireNonNull(spentAt, "지출일은 필수입니다");
        this.memo = trimmedMemo;
    }

    /** 아직 저장되지 않은 새 지출 */
    public static Expense create(long amount, String category, LocalDate spentAt, String memo) {
        return new Expense(null, amount, category, spentAt, memo);
    }

    /** 저장소에서 다시 불러온 지출 */
    public static Expense of(Long id, long amount, String category, LocalDate spentAt, String memo) {
        return new Expense(Objects.requireNonNull(id), amount, category, spentAt, memo);
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
