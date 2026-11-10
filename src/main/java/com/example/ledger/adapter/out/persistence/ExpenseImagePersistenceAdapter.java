package com.example.ledger.adapter.out.persistence;

import com.example.ledger.application.port.out.ExpenseImagePort;
import com.example.ledger.domain.ExpenseImage;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class ExpenseImagePersistenceAdapter implements ExpenseImagePort {

    private final ExpenseImageJpaRepository repository;

    ExpenseImagePersistenceAdapter(ExpenseImageJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveAll(long expenseId, List<ExpenseImage> images) {
        repository.saveAll(images.stream()
                .map(image -> new ExpenseImageJpaEntity(expenseId, image.getContentType(), image.getData()))
                .toList());
    }

    @Override
    public Map<Long, List<Long>> loadImageIds(Collection<Long> expenseIds) {
        if (expenseIds.isEmpty()) {
            return Map.of();
        }
        return repository.findByExpenseIdInOrderByIdAsc(expenseIds).stream()
                .collect(Collectors.groupingBy(ExpenseImageJpaRepository.ImageRef::getExpenseId,
                        Collectors.mapping(ExpenseImageJpaRepository.ImageRef::getId, Collectors.toList())));
    }

    @Override
    public Optional<ExpenseImage> load(long imageId) {
        return repository.findById(imageId)
                .map(entity -> new ExpenseImage(entity.getContentType(), entity.getData()));
    }

    @Override
    public void deleteByExpenseIds(Collection<Long> expenseIds) {
        repository.deleteByExpenseIdIn(expenseIds);
    }
}
