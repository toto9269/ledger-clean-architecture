package com.example.ledger.repository.expense;

import com.example.ledger.entity.expense.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findBySpentAtBetweenOrderBySpentAtAscIdAsc(LocalDate from, LocalDate to);

    /** 기간 안 지출 금액의 합. 지출이 없으면 0 */
    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.spentAt between :from and :to")
    long sumAmountBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** 한 번의 DELETE 문으로 지우고, 실제로 지운 건수를 돌려준다. */
    @Modifying
    @Query("delete from Expense e where e.id in :ids")
    int deleteByIdIn(@Param("ids") Collection<Long> ids);
}
