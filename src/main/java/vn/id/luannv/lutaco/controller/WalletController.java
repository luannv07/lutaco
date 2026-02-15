package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(
        name = "Wallet",
        description = "API quản lý wallet/ngân sách cá nhân của người dùng"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class WalletController {

    WalletService walletService;

    @PostMapping
    @Operation(
            summary = "Tạo wallet mới",
            description = "Người dùng tạo wallet mới theo giới hạn của gói dịch vụ"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo ví thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Vượt quá giới hạn số lượng ví")
    })
    public ResponseEntity<BaseResponse<Wallet>> create(
            @Valid @RequestBody WalletCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        walletService.create(request),
                        "Tạo ví thành công."
                ));
    }

    @PutMapping("/{walletName}")
    @Operation(
            summary = "Cập nhật wallet",
            description = "Cập nhật tên hoặc mô tả wallet của chính người dùng"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật ví thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ví")
    })
    public ResponseEntity<BaseResponse<Wallet>> update(
            @Parameter(
                    description = "Tên wallet cần cập nhật",
                    example = "personal-wallet",
                    required = true
            )
            @PathVariable String walletName,
            @Valid @RequestBody WalletUpdateRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.update(walletName, request),
                        "Cập nhật ví thành công."
                ));
    }

    @DeleteMapping("/{walletName}")
    @Operation(
            summary = "Xoá wallet",
            description = "Người dùng xoá wallet của mình (chuyển trạng thái sang INACTIVE)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa ví thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ví")
    })
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(
                    description = "Tên wallet cần xoá",
                    example = "personal-wallet",
                    required = true
            )
            @PathVariable String walletName
    ) {
        walletService.deleteByUser(walletName);
        return ResponseEntity.ok(
                BaseResponse.success("Xóa ví thành công.")
        );
    }

    @GetMapping("/{walletName}")
    @Operation(
            summary = "Lấy chi tiết wallet",
            description = "Lấy thông tin chi tiết một wallet thuộc quyền sở hữu của user hiện tại"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết ví thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ví")
    })
    public ResponseEntity<BaseResponse<Wallet>> getDetail(
            @Parameter(
                    description = "Tên wallet cần lấy thông tin",
                    example = "personal-wallet",
                    required = true
            )
            @PathVariable String walletName
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.getDetail(walletName),
                        "Lấy chi tiết ví thành công."
                ));
    }

    @GetMapping
    @Operation(
            summary = "Lấy danh sách wallet của tôi",
            description = "Lấy toàn bộ wallet của user đang đăng nhập"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách ví thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<BaseResponse<List<Wallet>>> getMyWallets() {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.getMyWallets(),
                        "Lấy danh sách ví thành công."
                ));
    }
}
