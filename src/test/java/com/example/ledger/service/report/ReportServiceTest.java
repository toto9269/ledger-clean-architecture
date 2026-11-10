package com.example.ledger.service.report;

import com.example.ledger.dto.expense.AddExpenseCommand;
import com.example.ledger.dto.report.MonthlyReport;
import com.example.ledger.service.budget.BudgetService;
import com.example.ledger.service.expense.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 다른 테스트와 데이터가 섞이지 않게 2025년 8월 이후를 쓴다. */
@SpringBootTest
class ReportServiceTest {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private BudgetService budgetService;

    @Test
    void 한_달_합계와_카테고리별_합계를_계산하고_다른_달은_섞지_않는다() {
        add("2025-08-15", 12_000, "식비");
        add("2025-08-15", 3_000, "교통비");
        add("2025-08-20", 8_000, "식비");
        add("2025-09-01", 1_000, "식비");
        budgetService.set(YearMonth.of(2025, 8), 30_000);

        MonthlyReport report = reportService.get(YearMonth.of(2025, 8));

        assertThat(report.total()).isEqualTo(23_000);
        assertThat(report.totalByCategory()).isEqualTo(Map.of("식비", 20_000L, "교통비", 3_000L));
        assertThat(report.expenses()).hasSize(3);
        assertThat(report.budgetLimit()).isEqualTo(30_000);
        assertThat(report.remaining()).isEqualTo(7_000);
    }

    @Test
    void 예산이_없는_달은_예산과_남은_금액이_없다() {
        add("2025-10-01", 5_000, "기타");

        MonthlyReport report = reportService.get(YearMonth.of(2025, 10));

        assertThat(report.budgetLimit()).isNull();
        assertThat(report.remaining()).isNull();
    }

    private void add(String date, long amount, String category) {
        expenseService.add(new AddExpenseCommand(amount, category, LocalDate.parse(date), null, List.of()));
    }
}
