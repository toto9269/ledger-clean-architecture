package com.example.ledger.adapter.in.web;

import com.example.ledger.application.port.in.GetMonthlyReportUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
class ReportController {

    private final GetMonthlyReportUseCase getMonthlyReportUseCase;

    ReportController(GetMonthlyReportUseCase getMonthlyReportUseCase) {
        this.getMonthlyReportUseCase = getMonthlyReportUseCase;
    }

    @GetMapping("/{month}")
    GetMonthlyReportUseCase.Report get(@PathVariable YearMonth month) {
        return getMonthlyReportUseCase.get(month);
    }
}
