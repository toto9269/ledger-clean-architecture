package com.example.ledger.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/** 지출 이미지 테이블. 이미지 내용은 BLOB 으로 DB 에 직접 저장한다. */
@Entity
@Table(name = "expense_image")
class ExpenseImageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long expenseId;

    @Column(nullable = false)
    private String contentType;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    protected ExpenseImageJpaEntity() {
    }

    ExpenseImageJpaEntity(Long expenseId, String contentType, byte[] data) {
        this.expenseId = expenseId;
        this.contentType = contentType;
        this.data = data;
    }

    String getContentType() {
        return contentType;
    }

    byte[] getData() {
        return data;
    }
}
