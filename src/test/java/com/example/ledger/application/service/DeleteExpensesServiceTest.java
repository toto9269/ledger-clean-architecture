package com.example.ledger.application.service;

import com.example.ledger.application.port.in.DeleteExpensesUseCase;
import com.example.ledger.application.port.out.DeleteExpensePort;
import com.example.ledger.application.port.out.ExpenseImagePort;
import com.example.ledger.domain.ExpenseImage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 삭제 순서와 검증만 본다. 실제 DB 롤백은 LedgerApiTest 가 확인한다. */
class DeleteExpensesServiceTest {

    private final List<String> calls = new ArrayList<>();
    private int existing = 2;

    private final DeleteExpensePort expenses = ids -> {
        calls.add("expenses" + ids);
        return Math.min(ids.size(), existing);
    };
    private final ExpenseImagePort images = new ExpenseImagePort() {
        @Override
        public void saveAll(long expenseId, List<ExpenseImage> newImages) {
        }

        @Override
        public Map<Long, List<Long>> loadImageIds(Collection<Long> expenseIds) {
            return Map.of();
        }

        @Override
        public Optional<ExpenseImage> load(long imageId) {
            return Optional.empty();
        }

        @Override
        public void deleteByExpenseIds(Collection<Long> expenseIds) {
            calls.add("images" + expenseIds);
        }
    };
    private final DeleteExpensesUseCase deleteExpenses = new DeleteExpensesService(expenses, images);

    @Test
    void 이미지를_먼저_지우고_지출을_지운다() {
        int deleted = deleteExpenses.delete(List.of(1L, 2L, 2L));

        assertThat(deleted).isEqualTo(2);
        assertThat(calls).containsExactly("images[1, 2]", "expenses[1, 2]");
    }

    @Test
    void 아무것도_고르지_않으면_거부한다() {
        assertThatThrownBy(() -> deleteExpenses.delete(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(calls).isEmpty();
    }

    @Test
    void 없는_지출이_섞이면_거부한다() {
        existing = 1;

        assertThatThrownBy(() -> deleteExpenses.delete(List.of(1L, 999L)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
