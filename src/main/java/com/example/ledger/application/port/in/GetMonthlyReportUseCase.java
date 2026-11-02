package com.example.ledger.application.port.in;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/** 월별 리포트 조회 유스케이스 */
public interface GetMonthlyReportUseCase {

    Report get(YearMonth month);

    /** budgetLimit·remaining 은 예산을 설정하지 않은 달이면 null */
    record Report(
            YearMonth month,
            long total,
            Long budgetLimit,
            Long remaining,
            Map<String, Long> totalByCategory,
            List<Item> expenses) {
    }

    record Item(long id, long amount, String category, LocalDate spentAt) {
    }
}
