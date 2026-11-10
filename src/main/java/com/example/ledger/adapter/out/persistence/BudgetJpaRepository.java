package com.example.ledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface BudgetJpaRepository extends JpaRepository<BudgetJpaEntity, String> {
}
