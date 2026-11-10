package com.example.ledger.application.service;

import com.example.ledger.application.port.in.DeleteExpensesUseCase;
import com.example.ledger.application.port.out.DeleteExpensePort;
import com.example.ledger.application.port.out.ExpenseImagePort;

import java.util.List;
import java.util.Objects;

/**
 * 지출 여러 건과 딸린 이미지를 지운다.
 * 여러 번의 삭제를 하나로 묶는 트랜잭션은 조립 지점(UseCaseConfig)이 감싼다.
 */
public class DeleteExpensesService implements DeleteExpensesUseCase {

    private final DeleteExpensePort deleteExpensePort;
    private final ExpenseImagePort expenseImagePort;

    public DeleteExpensesService(DeleteExpensePort deleteExpensePort, ExpenseImagePort expenseImagePort) {
        this.deleteExpensePort = deleteExpensePort;
        this.expenseImagePort = expenseImagePort;
    }

    @Override
    public int delete(List<Long> expenseIds) {
        List<Long> ids = expenseIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("삭제할 지출을 선택하세요");
        }

        // 딸린 이미지를 먼저 지워, 지출 없이 이미지만 남는 행이 생기지 않게 한다
        expenseImagePort.deleteByExpenseIds(ids);
        int deleted = deleteExpensePort.deleteAll(ids);
        if (deleted != ids.size()) {
            // 트랜잭션 안이므로 여기서 던지면 위의 삭제가 모두 되돌려진다
            throw new IllegalArgumentException("이미 삭제됐거나 없는 지출이 포함되어 있습니다");
        }
        return deleted;
    }
}
