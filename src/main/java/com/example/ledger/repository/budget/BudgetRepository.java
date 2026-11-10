package com.example.ledger.repository.budget;

import com.example.ledger.entity.budget.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, String> {

    /** 기본 키가 "2026-10" 같은 문자열이라, 연월로 바로 찾을 수 있게 감싼다. */
    default Optional<Budget> findByMonth(YearMonth month) {
        return findById(month.toString());
    }
}
