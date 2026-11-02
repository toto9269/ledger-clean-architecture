package com.example.ledger.adapter.in.web;

import com.example.ledger.application.port.in.AddExpenseUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

/** HTTP 요청을 유스케이스 명령으로 바꿔 넘긴다. 비즈니스 규칙은 두지 않는다. */
@RestController
@RequestMapping("/api/expenses")
class ExpenseController {

    private final AddExpenseUseCase addExpenseUseCase;
    private final Clock clock;

    ExpenseController(AddExpenseUseCase addExpenseUseCase, Clock clock) {
        this.addExpenseUseCase = addExpenseUseCase;
        this.clock = clock;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AddExpenseUseCase.Result add(@RequestBody AddExpenseRequest request) {
        if (request.amount() == null) {
            throw new IllegalArgumentException("지출 금액은 필수입니다");
        }
        LocalDate spentAt = request.spentAt() != null ? request.spentAt() : LocalDate.now(clock);
        return addExpenseUseCase.add(
                new AddExpenseUseCase.Command(request.amount(), request.category(), spentAt));
    }

    /** spentAt 을 비우면 오늘 날짜로 등록한다. */
    record AddExpenseRequest(Long amount, String category, LocalDate spentAt) {
    }
}
