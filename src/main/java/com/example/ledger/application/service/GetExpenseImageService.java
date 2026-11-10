package com.example.ledger.application.service;

import com.example.ledger.application.port.in.GetExpenseImageUseCase;
import com.example.ledger.application.port.out.ExpenseImagePort;

import java.util.Optional;

public class GetExpenseImageService implements GetExpenseImageUseCase {

    private final ExpenseImagePort expenseImagePort;

    public GetExpenseImageService(ExpenseImagePort expenseImagePort) {
        this.expenseImagePort = expenseImagePort;
    }

    @Override
    public Optional<Image> get(long imageId) {
        return expenseImagePort.load(imageId)
                .map(image -> new Image(image.getContentType(), image.getData()));
    }
}
