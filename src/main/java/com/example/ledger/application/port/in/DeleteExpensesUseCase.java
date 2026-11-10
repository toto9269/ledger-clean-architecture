package com.example.ledger.application.port.in;

import java.util.List;

/** 지출 여러 건 삭제 유스케이스. 딸린 이미지도 함께 지운다. */
public interface DeleteExpensesUseCase {

    /** 지운 건수를 돌려준다. 없는 지출이 하나라도 섞이면 아무것도 지우지 않는다. */
    int delete(List<Long> expenseIds);
}
