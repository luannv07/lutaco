package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.service.PayOsService;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
@Tag(
        name = "Payment / PayOS",
        description = "API thanh toán tích hợp PayOS (nâng cấp tài khoản, truy vấn giao dịch)"
)
public class PayOSController {

    PayOsService payOsService;

    @PostMapping("/premium-user")
    @Operation(
            summary = "Tạo giao dịch nâng cấp Premium",
            description = "Khởi tạo giao dịch thanh toán PayOS để nâng cấp tài khoản người dùng lên Premium"
    )
    public ResponseEntity<BaseResponse<PayOSResponse<PayOSResponse.PayOSDataCreated>>> createPayment() {
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success(
                                payOsService.createPayment(PaymentType.UPGRADE_PREMIUM),
                                MessageKeyConst.Success.CREATED
                        )
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("(hasRole('SYS_ADMIN') or hasRole('ADMIN'))")
    @Operation(
            summary = "Lấy chi tiết giao dịch PayOS",
            description = "Truy vấn thông tin chi tiết của một giao dịch PayOS theo mã giao dịch"
    )
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
                                MessageKeyConst.Success.CREATED
                        )
                );
    }
}
