package com.example.ledger.service.expense;

import com.example.ledger.dto.expense.AddExpenseCommand;
import com.example.ledger.dto.expense.ExpenseSaveResult;
import com.example.ledger.dto.expense.ImageFile;
import com.example.ledger.dto.expense.UpdateExpenseCommand;
import com.example.ledger.repository.expense.ExpenseImageRepository;
import com.example.ledger.repository.expense.ExpenseRepository;
import com.example.ledger.service.budget.BudgetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실제 DB(H2)와 트랜잭션으로 서비스를 검증한다. 예산 초과 알림은 로그 출력으로 확인한다.
 * 다른 테스트와 데이터가 섞이지 않게 테스트마다 2025년의 다른 달을 쓴다.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private BudgetService budgetService;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ExpenseImageRepository expenseImageRepository;

    @Test
    void 예산_안이면_알리지_않는다(CapturedOutput output) {
        budgetService.set(YearMonth.of(2025, 1), 50_000);

        ExpenseSaveResult result = expenseService.add(command("2025-01-15", 30_000, List.of()));

        assertThat(result.monthlyTotal()).isEqualTo(30_000);
        assertThat(result.budgetExceeded()).isFalse();
        assertThat(output.getOut()).doesNotContain("[예산 초과] 2025-01");
    }

    @Test
    void 예산을_넘는_순간_알린다(CapturedOutput output) {
        budgetService.set(YearMonth.of(2025, 2), 50_000);
        expenseService.add(command("2025-02-10", 30_000, List.of()));

        ExpenseSaveResult result = expenseService.add(command("2025-02-11", 25_000, List.of()));

        assertThat(result.budgetExceeded()).isTrue();
        assertThat(output.getOut()).contains("[예산 초과] 2025-02 예산 50000원, 현재 지출 55000원 (5000원 초과)");
    }

    @Test
    void 예산이_없는_달은_알리지_않는다() {
        ExpenseSaveResult result = expenseService.add(command("2025-03-01", 1_000_000, List.of()));

        assertThat(result.budgetExceeded()).isFalse();
    }

    @Test
    void 이미지를_함께_저장한다() {
        ExpenseSaveResult result = expenseService.add(command("2025-04-01", 1_000, List.of(png(), png())));

        assertThat(expenseImageRepository.findByExpenseIdInOrderByIdAsc(List.of(result.expenseId()))).hasSize(2);
    }

    @Test
    void 이미지가_잘못되면_지출도_저장하지_않는다() {
        List<ImageFile> images = List.of(png(), new ImageFile("text/html", new byte[]{1}));

        assertThatThrownBy(() -> expenseService.add(command("2025-05-01", 1_000, images)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(expensesIn(YearMonth.of(2025, 5))).isEmpty();
    }

    @Test
    void 변경으로_예산을_넘으면_알린다(CapturedOutput output) {
        budgetService.set(YearMonth.of(2025, 6), 20_000);
        long id = expenseService.add(command("2025-06-01", 10_000, List.of())).expenseId();

        ExpenseSaveResult result = expenseService.update(
                new UpdateExpenseCommand(id, 25_000, "식비", LocalDate.parse("2025-06-01"), null));

        assertThat(result.monthlyTotal()).isEqualTo(25_000);
        assertThat(result.budgetExceeded()).isTrue();
        assertThat(output.getOut()).contains("[예산 초과] 2025-06");
    }

    @Test
    void 없는_지출은_변경할_수_없다() {
        assertThatThrownBy(() -> expenseService.update(
                new UpdateExpenseCommand(999_999, 1_000, "식비", LocalDate.parse("2025-06-01"), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 삭제됐거나 없는 지출입니다");
    }

    @Test
    void 아무것도_고르지_않으면_삭제를_거부한다() {
        assertThatThrownBy(() -> expenseService.delete(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 없는_지출이_섞이면_삭제가_모두_되돌려진다() {
        long id = expenseService.add(command("2025-07-01", 1_000, List.of(png()))).expenseId();

        assertThatThrownBy(() -> expenseService.delete(List.of(id, 999_999L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(expenseRepository.existsById(id)).isTrue();
        assertThat(expenseImageRepository.findByExpenseIdInOrderByIdAsc(List.of(id))).hasSize(1);
    }

    private List<?> expensesIn(YearMonth month) {
        return expenseRepository.findBySpentAtBetweenOrderBySpentAtAscIdAsc(month.atDay(1), month.atEndOfMonth());
    }

    private static AddExpenseCommand command(String date, long amount, List<ImageFile> images) {
        return new AddExpenseCommand(amount, "식비", LocalDate.parse(date), null, images);
    }

    private static ImageFile png() {
        return new ImageFile("image/png", new byte[]{1, 2, 3});
    }
}
