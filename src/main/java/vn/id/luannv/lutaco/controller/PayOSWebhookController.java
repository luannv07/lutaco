package vn.id.luannv.lutaco.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.PayOsWebhookRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.service.PayOsClient;
import vn.id.luannv.lutaco.service.PayOsWebhookService;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class PayOSWebhookController {
    PayOsWebhookService payOsWebhookService;
    PayOsClient payOsClient;

    /**
     * Hứng result thông qua webhook sau khi chuyển khoản xong
     * <a href="https://api-merchant.payos.vn/confirm-webhook">API confirm</a>
     * {
     * "webhookUrl": "<a href="https://uncombed-semasiologically-leighton.ngrok-free.dev/webhook/payos">Server link đã được dev trên ngrok</a>"
     * }
     *
     * @param request body hứng request từ phía payos gửi về
     * @return trả về một message thành công, hoặc throw nếu như ko validate được signature
     */
    @PostMapping("/webhook/payos")
    public ResponseEntity<BaseResponse<Void>> handleWebhook(
            @RequestBody PayOsWebhookRequest request
    ) {
        log.info("handleWebhook received request={}", request);
        payOsWebhookService.handle(request);
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success(null, MessageKeyConst.Success.SUCCESS)
                );
    }

    @PostMapping("/api/v1/confirm-webhook")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> handleConfirmWebhook(@RequestBody Map<String, Object> confirm) {
        log.info("handleConfirmWebhook send request={}", confirm);
        payOsClient.confirmHookUrl(confirm);
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success(null, MessageKeyConst.Success.SUCCESS)
                );
    }

}
