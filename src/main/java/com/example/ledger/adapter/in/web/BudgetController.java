package com.example.ledger.adapter.in.web;

import com.example.ledger.application.port.in.SetBudgetUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
class BudgetController {

    private final SetBudgetUseCase setBudgetUseCase;

    BudgetController(SetBudgetUseCase setBudgetUseCase) {
        this.setBudgetUseCase = setBudgetUseCase;
    }

    @PutMapping("/{month}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void set(@PathVariable YearMonth month, @RequestBody SetBudgetRequest request) {
        if (request.limit() == null) {
            throw new IllegalArgumentException("예산 금액은 필수입니다");
        }
        setBudgetUseCase.set(month, request.limit());
    }

    record SetBudgetRequest(Long limit) {
    }
}
