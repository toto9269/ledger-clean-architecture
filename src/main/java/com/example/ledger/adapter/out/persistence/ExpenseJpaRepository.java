package com.example.ledger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

interface ExpenseJpaRepository extends JpaRepository<ExpenseJpaEntity, Long> {

    List<ExpenseJpaEntity> findBySpentAtBetweenOrderBySpentAtAscIdAsc(LocalDate from, LocalDate to);

    /** 한 번의 DELETE 문으로 지우고, 실제로 지운 건수를 돌려준다. */
    @Modifying
    @Query("delete from ExpenseJpaEntity e where e.id in :ids")
    int deleteByIdIn(@Param("ids") Collection<Long> ids);
}
