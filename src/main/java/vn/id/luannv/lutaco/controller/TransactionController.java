package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.service.TransactionService;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Transaction API", description = "API quản lý giao dịch")
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
/**
 * Toàn bộ các api bên dưới đều thao tác với data của chính bản thân
 */
public class TransactionController {

    TransactionService transactionService;

    @Operation(summary = "Lấy danh sách giao dịch")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<TransactionResponse>>> search(
            @ModelAttribute TransactionFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        transactionService.search(
                                request,
                                request.getPage(),
                                request.getSize()
                        ),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @Operation(summary = "Lấy chi tiết giao dịch")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponse>> getDetail(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        transactionService.getDetail(id),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @Operation(summary = "Tạo giao dịch")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(
            @Valid @RequestBody TransactionRequest request
    ) {
        transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(null, MessageKeyConst.Success.CREATED));
    }

    @Operation(summary = "Cập nhật giao dịch")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request
    ) {
        transactionService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success(null, MessageKeyConst.Success.UPDATED)
        );
    }

    @Operation(summary = "Xoá giao dịch (soft delete)")
    @PatchMapping("/{id}/disabled")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable String id
    ) {
        transactionService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success(null, MessageKeyConst.Success.UPDATED)
        );
    }
}
