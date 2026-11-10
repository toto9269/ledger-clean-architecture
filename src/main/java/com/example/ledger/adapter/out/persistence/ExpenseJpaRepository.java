package com.example.ledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

interface ExpenseJpaRepository extends JpaRepository<ExpenseJpaEntity, Long> {

    List<ExpenseJpaEntity> findBySpentAtBetweenOrderBySpentAtAscIdAsc(LocalDate from, LocalDate to);
}
