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

    /** memo 는 입력하지 않았으면 null, imageIds 는 이미지가 없으면 빈 목록 */
    record Item(long id, long amount, String category, LocalDate spentAt, String memo, List<Long> imageIds) {
    }
}
