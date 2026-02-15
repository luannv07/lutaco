package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.service.PayOsService;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
@Tag(
        name = "PayOS",
        description = "API thanh toán tích hợp PayOS (nâng cấp tài khoản, truy vấn giao dịch)"
)
public class PayOSController {

    PayOsService payOsService;

    @PostMapping("/premium-user")
    @Operation(
            summary = "Tạo giao dịch nâng cấp Premium",
            description = "Khởi tạo giao dịch thanh toán PayOS để nâng cấp tài khoản người dùng lên Premium"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thanh toán thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Tài khoản đã là Premium")
    })
    public ResponseEntity<BaseResponse<PayOSResponse<PayOSResponse.PayOSDataCreated>>> createPayment() {
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success(
                                payOsService.createPayment(PaymentType.UPGRADE_PREMIUM),
                                "Tạo thanh toán thành công."
                        )
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("(hasRole('SYS_ADMIN') or hasRole('ADMIN'))")
    @Operation(
            summary = "Lấy chi tiết giao dịch PayOS",
            description = "Truy vấn thông tin chi tiết của một giao dịch PayOS theo mã giao dịch"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thanh toán thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy giao dịch")
    })
    public ResponseEntity<BaseResponse<PayOSResponse<PayOSResponse.PayOSDataDetail>>> getDetail(
            @Parameter(
                    description = "Mã giao dịch PayOS",
                    example = "PAYOS_123456",
                    required = true
            )
            @PathVariable String id
    ) {
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success(
                                payOsService.getDetailPayment(id),
                                "Lấy chi tiết thanh toán thành công."
                        )
                );
    }
}
