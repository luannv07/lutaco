package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.service.RecurringTransactionService;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class RecurringTransactionController {

    RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<RecurringTransactionResponse>>> search(
            @Valid @ModelAttribute RecurringTransactionFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        recurringTransactionService.search(
                                request,
                                request.getPage(),
                                request.getSize()
                        ),
                        "Lấy danh sách giao dịch định kỳ thành công."
                )
        );
    }

    @PostMapping
    public ResponseEntity<BaseResponse<RecurringTransactionResponse>> create(
            @Valid
            @RequestBody RecurringTransactionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        recurringTransactionService.create(request),
                        "Tạo giao dịch định kỳ thành công."
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<RecurringTransactionResponse>> getDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        recurringTransactionService.getDetail(id),
                        "Lấy chi tiết giao dịch định kỳ thành công."
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable Long id,
            @Valid
            @RequestBody RecurringTransactionRequest request
    ) {
        recurringTransactionService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật giao dịch định kỳ thành công.")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable Long id
    ) {
        recurringTransactionService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success("Xóa giao dịch định kỳ thành công.")
        );
    }
}
