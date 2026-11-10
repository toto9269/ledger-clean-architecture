package com.example.ledger.application.port.out;

import com.example.ledger.domain.ExpenseImage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 지출 이미지 저장·조회 */
public interface ExpenseImagePort {

    void saveAll(long expenseId, List<ExpenseImage> images);

    /** 지출 id 별 이미지 id 목록(올린 순서). 이미지 내용은 읽지 않는다. 이미지가 없는 지출은 빠진다. */
    Map<Long, List<Long>> loadImageIds(Collection<Long> expenseIds);

    Optional<ExpenseImage> load(long imageId);

    /** 지출에 딸린 이미지를 모두 지운다. */
    void deleteByExpenseIds(Collection<Long> expenseIds);
}
