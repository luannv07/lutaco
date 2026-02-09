package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.service.RecurringTransactionService;

@Slf4j
@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Recurring Transaction API",
        description = "API quản lý giao dịch định kỳ của người dùng"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class RecurringTransactionController {

    RecurringTransactionService recurringTransactionService;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách giao dịch định kỳ",
            description = "Lấy danh sách giao dịch định kỳ của người dùng hiện tại, hỗ trợ lọc theo nhiều tiêu chí và phân trang"
    )
    public ResponseEntity<BaseResponse<Page<RecurringTransactionResponse>>> search(
            @Parameter(description = "Điều kiện lọc và phân trang giao dịch định kỳ")
            @ModelAttribute RecurringTransactionFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        recurringTransactionService.search(
                                request,
                                request.getPage(),
                                request.getSize()
                        ),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Tạo giao dịch định kỳ mới",
            description = "Tạo mới một giao dịch định kỳ cho người dùng hiện tại"
    )
    public ResponseEntity<BaseResponse<RecurringTransactionResponse>> create(
            @Valid
            @Parameter(description = "Thông tin giao dịch định kỳ cần tạo")
            @RequestBody RecurringTransactionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(recurringTransactionService.create(request), MessageKeyConst.Success.CREATED));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết giao dịch định kỳ",
            description = "Lấy thông tin chi tiết của một giao dịch định kỳ theo id"
    )
    public ResponseEntity<BaseResponse<RecurringTransactionResponse>> getDetail(
            @Parameter(
                    description = "ID giao dịch định kỳ",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        recurringTransactionService.getDetail(id),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Cập nhật giao dịch định kỳ",
            description = "Cập nhật thông tin giao dịch định kỳ theo id"
    )
    public ResponseEntity<BaseResponse<Void>> update(
            @Parameter(
                    description = "ID giao dịch định kỳ",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @Valid
            @Parameter(description = "Thông tin giao dịch định kỳ cần cập nhật")
            @RequestBody RecurringTransactionRequest request
    ) {
        recurringTransactionService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success(null, MessageKeyConst.Success.UPDATED)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xoá giao dịch định kỳ",
            description = "Xoá giao dịch định kỳ khỏi hệ thống"
    )
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(
                    description = "ID giao dịch định kỳ",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        recurringTransactionService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success(null, MessageKeyConst.Success.DELETED)
        );
    }
}
