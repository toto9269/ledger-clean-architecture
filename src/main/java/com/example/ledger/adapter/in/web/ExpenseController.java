package com.example.ledger.adapter.in.web;

import com.example.ledger.application.port.in.AddExpenseUseCase;
import com.example.ledger.application.port.in.DeleteExpensesUseCase;
import com.example.ledger.application.port.in.GetExpenseImageUseCase;
import com.example.ledger.application.port.in.UpdateExpenseUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** HTTP 요청을 유스케이스 명령으로 바꿔 넘긴다. 비즈니스 규칙은 두지 않는다. */
@RestController
@RequestMapping("/api/expenses")
class ExpenseController {

    private final AddExpenseUseCase addExpenseUseCase;
    private final GetExpenseImageUseCase getExpenseImageUseCase;
    private final DeleteExpensesUseCase deleteExpensesUseCase;
    private final UpdateExpenseUseCase updateExpenseUseCase;
    private final Clock clock;

    ExpenseController(AddExpenseUseCase addExpenseUseCase, GetExpenseImageUseCase getExpenseImageUseCase,
                      DeleteExpensesUseCase deleteExpensesUseCase, UpdateExpenseUseCase updateExpenseUseCase,
                      Clock clock) {
        this.addExpenseUseCase = addExpenseUseCase;
        this.getExpenseImageUseCase = getExpenseImageUseCase;
        this.deleteExpensesUseCase = deleteExpensesUseCase;
        this.updateExpenseUseCase = updateExpenseUseCase;
        this.clock = clock;
    }

    /** 금액·카테고리·날짜·메모를 바꾼다. 이미지는 그대로 둔다. */
    @PutMapping("/{expenseId}")
    UpdateExpenseUseCase.Result update(@PathVariable long expenseId, @RequestBody UpdateExpenseRequest request) {
        if (request.amount() == null) {
            throw new IllegalArgumentException("지출 금액은 필수입니다");
        }
        if (request.spentAt() == null) {
            throw new IllegalArgumentException("지출일은 필수입니다");
        }
        return updateExpenseUseCase.update(new UpdateExpenseUseCase.Command(
                expenseId, request.amount(), request.category(), request.spentAt(), request.memo()));
    }

    /** 여러 건을 한 번에 지운다. 딸린 이미지도 함께 지워진다. 예: DELETE /api/expenses?ids=1,2,3 */
    @DeleteMapping
    Map<String, Integer> delete(@RequestParam(required = false) List<Long> ids) {
        return Map.of("deleted", deleteExpensesUseCase.delete(ids == null ? List.of() : ids));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AddExpenseUseCase.Result add(@RequestBody AddExpenseRequest request) {
        return add(request.amount(), request.category(), request.spentAt(), request.memo(), List.of());
    }

    /** 이미지와 함께 등록할 때는 multipart 로 받는다. 이미지 파트 이름은 images (여러 개) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    AddExpenseUseCase.Result addWithImages(@RequestParam(required = false) Long amount,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate spentAt,
                                           @RequestParam(required = false) String memo,
                                           @RequestParam(required = false) List<MultipartFile> images) throws IOException {
        List<AddExpenseUseCase.ImageFile> files = new ArrayList<>();
        for (MultipartFile image : images == null ? List.<MultipartFile>of() : images) {
            files.add(new AddExpenseUseCase.ImageFile(image.getContentType(), image.getBytes()));
        }
        return add(amount, category, spentAt, memo, files);
    }

    private AddExpenseUseCase.Result add(Long amount, String category, LocalDate spentAt, String memo,
                                         List<AddExpenseUseCase.ImageFile> images) {
        if (amount == null) {
            throw new IllegalArgumentException("지출 금액은 필수입니다");
        }
        LocalDate date = spentAt != null ? spentAt : LocalDate.now(clock);
        return addExpenseUseCase.add(new AddExpenseUseCase.Command(amount, category, date, memo, images));
    }

    /** 이미지는 한 번 올리면 바뀌지 않으므로 브라우저가 캐시하게 한다. */
    @GetMapping("/images/{imageId}")
    ResponseEntity<byte[]> image(@PathVariable long imageId) {
        return getExpenseImageUseCase.get(imageId)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                        .header("X-Content-Type-Options", "nosniff")
                        .body(image.data()))
                .orElse(ResponseEntity.notFound().build());
    }

    /** spentAt 을 비우면 오늘 날짜로 등록한다. memo 는 선택이다. */
    record AddExpenseRequest(Long amount, String category, LocalDate spentAt, String memo) {
    }

    record UpdateExpenseRequest(Long amount, String category, LocalDate spentAt, String memo) {
    }
}
