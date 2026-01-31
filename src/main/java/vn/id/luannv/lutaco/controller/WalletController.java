package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.WalletCreateRequest;
import vn.id.luannv.lutaco.dto.request.WalletUpdateRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.service.WalletService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Wallet API", description = "API quản lý ngân sách cá nhân")
@PreAuthorize("isAuthenticated()")
public class WalletController {

    WalletService walletService;

    @Operation(
            summary = "Tạo wallet mới",
            description = "Người dùng tạo wallet mới (giới hạn theo user plan)"
    )
    @PostMapping
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Wallet>> create(
            @Valid @RequestBody WalletCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        walletService.create(request),
                        MessageKeyConst.Success.CREATED
                ));
    }

    @Operation(
            summary = "Cập nhật wallet",
            description = "Chỉnh sửa tên hoặc mô tả wallet của chính mình"
    )
    @PutMapping("/{walletName}")
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Wallet>> update(
            @Parameter(description = "Tên wallet cần cập nhật")
            @PathVariable String walletName,
            @Valid @RequestBody WalletUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        walletService.update(walletName, request),
                        MessageKeyConst.Success.UPDATED
                ));
    }

    @Operation(
            summary = "Xoá wallet (user)",
            description = "Người dùng xoá wallet của mình (chuyển sang INACTIVE)"
    )
    @DeleteMapping("/{walletName}")
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(description = "Tên wallet cần xoá")
            @PathVariable String walletName
    ) {
        walletService.deleteByUser(walletName);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        null,
                        MessageKeyConst.Success.DELETED
                ));
    }

    @Operation(
            summary = "Lấy chi tiết wallet",
            description = "Lấy thông tin chi tiết một wallet của chính mình"
    )
    @GetMapping("/{walletName}")
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Wallet>> getDetail(
            @Parameter(description = "Tên wallet cần lấy")
            @PathVariable String walletName
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        walletService.getDetail(walletName),
                        MessageKeyConst.Success.SENT
                ));
    }

    @Operation(
            summary = "Lấy danh sách wallet của tôi",
            description = "Lấy toàn bộ wallet của user hiện tại"
    )
    @GetMapping
    @PreAuthorize("hasRole('USER') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<List<Wallet>>> getMyWallets() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        walletService.getMyWallets(),
                        MessageKeyConst.Success.SENT
                ));
    }

}
