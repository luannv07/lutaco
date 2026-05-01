package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.dto.request.PayOsWebhookRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.service.PayOsClient;
import vn.id.luannv.lutaco.service.PayOsWebhookService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSWebhookController {

    PayOsWebhookService payOsWebhookService;
    PayOsClient payOsClient;

    @PostMapping("/webhook/payos")
    public ResponseEntity<BaseResponse<Void>> handleWebhook(
            @Valid @RequestBody PayOsWebhookRequest request
    ) {
        log.info("[system]: Received PayOS webhook request: {}", request);
        try {
            payOsWebhookService.handle(request);
            log.info("[system]: Successfully processed PayOS webhook for order code: {}", request.getData().getOrderCode());
        } catch (Exception e) {
            log.error("[system]: Error processing PayOS webhook for order code {}: {}", request.getData().getOrderCode(), e.getMessage(), e);
        }
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success("Xử lý webhook thành công.")
                );
    }

    @PostMapping("/api/v1/confirm-webhook")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> handleConfirmWebhook(
            @Valid @RequestBody Map<String, Object> confirm
    ) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to confirm PayOS webhook URL with request: {}", username, confirm);
        payOsClient.confirmHookUrl(confirm);
        log.info("[{}]: Successfully confirmed PayOS webhook URL.", username);
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success("Xác nhận webhook thành công.")
                );
    }
}
