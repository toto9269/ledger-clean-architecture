package com.example.ledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

interface ExpenseImageJpaRepository extends JpaRepository<ExpenseImageJpaEntity, Long> {

    /** 목록 화면용. 이미지 내용(BLOB)은 빼고 id 만 읽는다. */
    List<ImageRef> findByExpenseIdInOrderByIdAsc(Collection<Long> expenseIds);

    interface ImageRef {
        Long getId();

        Long getExpenseId();
    }
}
