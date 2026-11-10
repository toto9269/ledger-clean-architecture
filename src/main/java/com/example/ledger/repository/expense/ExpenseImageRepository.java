package com.example.ledger.repository.expense;

import com.example.ledger.entity.expense.ExpenseImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ExpenseImageRepository extends JpaRepository<ExpenseImage, Long> {

    /** 목록 화면용. 이미지 내용(BLOB)은 빼고 id 만 읽는다. */
    List<ImageRef> findByExpenseIdInOrderByIdAsc(Collection<Long> expenseIds);

    /** 이미지를 읽지 않고 한 번의 DELETE 문으로 지운다. */
    @Modifying
    @Query("delete from ExpenseImage i where i.expenseId in :expenseIds")
    int deleteByExpenseIdIn(@Param("expenseIds") Collection<Long> expenseIds);

    interface ImageRef {
        Long getId();

        Long getExpenseId();
    }
}
