package com.example.ledger.controller.expense;

import com.example.ledger.dto.expense.AddExpenseCommand;
import com.example.ledger.dto.expense.AddExpenseRequest;
import com.example.ledger.dto.expense.DeleteExpensesResponse;
import com.example.ledger.dto.expense.ExpenseSaveResponse;
import com.example.ledger.dto.expense.ImageFile;
import com.example.ledger.dto.expense.UpdateExpenseCommand;
import com.example.ledger.dto.expense.UpdateExpenseRequest;
import com.example.ledger.service.expense.ExpenseService;
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

/** HTTP 요청을 서비스 호출로 바꾼다. 규칙 검사는 엔티티와 서비스가 맡는다. */
@RestController
@RequestMapping("/api/expenses")
class ExpenseController {

    private final ExpenseService expenseService;
    private final Clock clock;

    ExpenseController(ExpenseService expenseService, Clock clock) {
        this.expenseService = expenseService;
        this.clock = clock;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ExpenseSaveResponse add(@RequestBody AddExpenseRequest request) {
        return add(request.amount(), request.category(), request.spentAt(), request.memo(), List.of());
    }

    /** 이미지와 함께 등록할 때는 multipart 로 받는다. 이미지 파트 이름은 images (여러 개) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ExpenseSaveResponse addWithImages(@RequestParam(required = false) Long amount,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate spentAt,
                                      @RequestParam(required = false) String memo,
                                      @RequestParam(required = false) List<MultipartFile> images) throws IOException {
        List<ImageFile> files = new ArrayList<>();
        for (MultipartFile image : images == null ? List.<MultipartFile>of() : images) {
            files.add(new ImageFile(image.getContentType(), image.getBytes()));
        }
        return add(amount, category, spentAt, memo, files);
    }

    private ExpenseSaveResponse add(Long amount, String category, LocalDate spentAt, String memo, List<ImageFile> images) {
        if (amount == null) {
            throw new IllegalArgumentException("지출 금액은 필수입니다");
        }
        LocalDate date = spentAt != null ? spentAt : LocalDate.now(clock);
        return ExpenseSaveResponse.from(expenseService.add(new AddExpenseCommand(amount, category, date, memo, images)));
    }

    /** 금액·카테고리·날짜·메모를 바꾼다. 이미지는 그대로 둔다. */
    @PutMapping("/{expenseId}")
    ExpenseSaveResponse update(@PathVariable long expenseId, @RequestBody UpdateExpenseRequest request) {
        if (request.amount() == null) {
            throw new IllegalArgumentException("지출 금액은 필수입니다");
        }
        if (request.spentAt() == null) {
            throw new IllegalArgumentException("지출일은 필수입니다");
        }
        return ExpenseSaveResponse.from(expenseService.update(new UpdateExpenseCommand(
                expenseId, request.amount(), request.category(), request.spentAt(), request.memo())));
    }

    /** 여러 건을 한 번에 지운다. 딸린 이미지도 함께 지워진다. 예: DELETE /api/expenses?ids=1,2,3 */
    @DeleteMapping
    DeleteExpensesResponse delete(@RequestParam(required = false) List<Long> ids) {
        return new DeleteExpensesResponse(expenseService.delete(ids == null ? List.of() : ids));
    }

    /** 이미지는 한 번 올리면 바뀌지 않으므로 브라우저가 캐시하게 한다. */
    @GetMapping("/images/{imageId}")
    ResponseEntity<byte[]> image(@PathVariable long imageId) {
        return expenseService.getImage(imageId)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                        .header("X-Content-Type-Options", "nosniff")
                        .body(image.data()))
                .orElse(ResponseEntity.notFound().build());
    }
}
