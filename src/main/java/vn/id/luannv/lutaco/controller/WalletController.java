package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.WalletCreateRequest;
import vn.id.luannv.lutaco.dto.request.WalletUpdateRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.WalletResponse;
import vn.id.luannv.lutaco.service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class WalletController {

    WalletService walletService;

    @PostMapping
            public ResponseEntity<BaseResponse<WalletResponse>> create(
            @Valid @RequestBody WalletCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        walletService.create(request),
                        "Tạo ví thành công."
                ));
    }

    @PutMapping("/{id}")
            public ResponseEntity<BaseResponse<WalletResponse>> update(
                        @PathVariable String id,
            @Valid @RequestBody WalletUpdateRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.update(id, request),
                        "Cập nhật ví thành công."
                ));
    }

    @PatchMapping("/{id}/toggle-status")
            public ResponseEntity<BaseResponse<Void>> toggle(
                        @PathVariable String id
    ) {
        walletService.toggle(id);
        return ResponseEntity.ok(
                BaseResponse.success("Kích hoạt/Huỷ kích hoạt ví thành công.")
        );
    }

    @GetMapping("/{id}")
            public ResponseEntity<BaseResponse<WalletResponse>> getDetail(
                        @PathVariable String id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.getDetail(id),
                        "Lấy chi tiết ví thành công."
                ));
    }

    @GetMapping
            public ResponseEntity<BaseResponse<List<WalletResponse>>> getMyWallets() {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.getMyWallets(),
                        "Lấy danh sách ví thành công."
                ));
    }
}
