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
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.service.TransactionService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class TransactionController {

    TransactionService transactionService;

    @GetMapping
            public ResponseEntity<BaseResponse<Page<TransactionResponse>>> search(
                        @Valid @ModelAttribute TransactionFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        transactionService.search(
                                request,
                                request.getPage(),
                                request.getSize()
                        ),
                        "Lấy danh sách giao dịch thành công."
                )
        );
    }

    @GetMapping("/{id}")
            public ResponseEntity<BaseResponse<TransactionResponse>> getDetail(
                        @PathVariable String id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        transactionService.getDetail(id),
                        "Lấy chi tiết giao dịch thành công."
                )
        );
    }

    @PostMapping
            public ResponseEntity<BaseResponse<TransactionResponse>> create(
            @Valid
                        @RequestBody TransactionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        transactionService.customCreate(request, SecurityUtils.getCurrentId()),
                        "Tạo giao dịch thành công."
                ));
    }

    @PostMapping("/bulk")
            public ResponseEntity<BaseResponse<List<TransactionResponse>>> createBulk(
            @Valid
                        @RequestBody List<TransactionRequest> requests
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        transactionService.createBulk(requests, SecurityUtils.getCurrentId()),
                        "Tạo hàng loạt giao dịch thành công."
                ));
    }

    @PutMapping("/bulk")
            public ResponseEntity<BaseResponse<Void>> deleteBulk(
            @Valid
                        @RequestBody List<String> ids
    ) {
        transactionService.deleteBulk(ids, SecurityUtils.getCurrentId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        "Tạo hàng loạt giao dịch thành công."
                ));
    }

    @PutMapping("/{id}")
            public ResponseEntity<BaseResponse<Void>> update(
                        @PathVariable String id,
            @Valid
                        @RequestBody TransactionRequest request
    ) {
        transactionService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật giao dịch thành công.")
        );
    }

    @PatchMapping("/{id}/{walletId}/disable")
            public ResponseEntity<BaseResponse<Void>> delete(
                        @PathVariable String id,
                        @PathVariable String walletId
    ) {
        transactionService.deleteByIdAndWalletId(id, walletId);
        return ResponseEntity.ok(
                BaseResponse.success("Xóa giao dịch thành công.")
        );
    }

    @PatchMapping("/{id}/{walletId}/enable")
            @PreAuthorize("@securityPermission.isPremiumUser()")
    public ResponseEntity<BaseResponse<Void>> unDelete(
                        @PathVariable String id,
                        @PathVariable String walletId
    ) {
        transactionService.restoreTransaction(id, walletId);
        return ResponseEntity.ok(
                BaseResponse.success("Phục hồi giao dịch thành công.")
        );
    }
}
