package com.example.ledger.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** DB 테이블 모양. 도메인 Expense 와 일부러 분리해 JPA 가 도메인으로 새지 않게 한다. */
@Entity
@Table(name = "expense")
class ExpenseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long amount;

    private String category;

    private LocalDate spentAt;

    @Column(length = 100)
    private String memo;

    protected ExpenseJpaEntity() {
    }

    ExpenseJpaEntity(long amount, String category, LocalDate spentAt, String memo) {
        this.amount = amount;
        this.category = category;
        this.spentAt = spentAt;
        this.memo = memo;
    }

    Long getId() {
        return id;
    }

    long getAmount() {
        return amount;
    }

    String getCategory() {
        return category;
    }

    LocalDate getSpentAt() {
        return spentAt;
    }

    String getMemo() {
        return memo;
    }
}
