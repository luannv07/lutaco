package vn.id.luannv.lutaco.controller;

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

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class PayOSController {

    PayOsService payOsService;

    @PostMapping("/premium-user")
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
    public ResponseEntity<BaseResponse<PayOSResponse<PayOSResponse.PayOSDataDetail>>> getDetail(
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

    @GetMapping("/users/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<BaseResponse<List<PayOSResponse.PayOSDataByUser>>> getByUserId(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success(
                                payOsService.getPaymentsByUserId(userId),
                                "Get payment transactions successfully."
                        )
                );
    }
}
