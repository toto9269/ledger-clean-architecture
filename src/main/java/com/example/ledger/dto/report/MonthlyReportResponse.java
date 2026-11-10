package com.example.ledger.dto.report;

import com.example.ledger.dto.report.MonthlyReport;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/** GET /api/reports/{yyyy-MM} 응답 */
public record MonthlyReportResponse(
        YearMonth month,
        long total,
        Long budgetLimit,
        Long remaining,
        Map<String, Long> totalByCategory,
        List<Item> expenses) {

    public record Item(long id, long amount, String category, LocalDate spentAt, String memo, List<Long> imageIds) {
    }

    public static MonthlyReportResponse from(MonthlyReport report) {
        return new MonthlyReportResponse(
                report.month(),
                report.total(),
                report.budgetLimit(),
                report.remaining(),
                report.totalByCategory(),
                report.expenses().stream()
                        .map(e -> new Item(e.id(), e.amount(), e.category(), e.spentAt(), e.memo(), e.imageIds()))
                        .toList());
    }
}
