package com.example.ledger.controller.budget;

import com.example.ledger.dto.budget.SetBudgetRequest;
import com.example.ledger.service.budget.BudgetService;
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

    private final BudgetService budgetService;

    BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PutMapping("/{month}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void set(@PathVariable YearMonth month, @RequestBody SetBudgetRequest request) {
        if (request.limit() == null) {
            throw new IllegalArgumentException("예산 금액은 필수입니다");
        }
        budgetService.set(month, request.limit());
    }
}
