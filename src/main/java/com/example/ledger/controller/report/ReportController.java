package com.example.ledger.controller.report;

import com.example.ledger.dto.report.MonthlyReportResponse;
import com.example.ledger.service.report.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
class ReportController {

    private final ReportService reportService;

    ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{month}")
    MonthlyReportResponse get(@PathVariable YearMonth month) {
        return MonthlyReportResponse.from(reportService.get(month));
    }
}
