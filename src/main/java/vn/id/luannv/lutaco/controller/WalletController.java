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
    public ResponseEntity<BaseResponse<WalletResponse>> update(
            @Parameter(
                    description = "Tên wallet cần cập nhật",
                    example = "personal-wallet",
                    required = true
            )
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
    @Operation(
            summary = "toggle wallet",
            description = "Người dùng toggle wallet của mình (chuyển trạng thái)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa ví thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ví")
    })
    public ResponseEntity<BaseResponse<Void>> toggle(
            @Parameter(
                    description = "Tên wallet cần toggle",
                    example = "personal-wallet",
                    required = true
            )
            @PathVariable String id
    ) {
        walletService.toggle(id);
        return ResponseEntity.ok(
                BaseResponse.success("Kích hoạt/Huỷ kích hoạt ví thành công.")
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết wallet",
            description = "Lấy thông tin chi tiết một wallet thuộc quyền sở hữu của user hiện tại"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết ví thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ví")
    })
    public ResponseEntity<BaseResponse<WalletResponse>> getDetail(
            @Parameter(
                    description = "Tên wallet cần lấy thông tin",
                    example = "personal-wallet",
                    required = true
            )
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.getDetail(id),
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
    public ResponseEntity<BaseResponse<List<WalletResponse>>> getMyWallets() {
        return ResponseEntity.ok(
                BaseResponse.success(
                        walletService.getMyWallets(),
                        "Lấy danh sách ví thành công."
                ));
    }
}
