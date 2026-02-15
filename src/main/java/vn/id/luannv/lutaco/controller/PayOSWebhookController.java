package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "PayOS Webhook",
        description = "API webhook nhận kết quả thanh toán từ PayOS và xác nhận webhook với PayOS"
)
public class PayOSWebhookController {

    PayOsWebhookService payOsWebhookService;
    PayOsClient payOsClient;

    @PostMapping("/webhook/payos")
    @Operation(
            summary = "Nhận webhook thanh toán PayOS",
            description = "Hứng kết quả thanh toán PayOS sau khi chuyển khoản hoàn tất. " +
                    "API sẽ validate chữ ký (signature) và xử lý trạng thái giao dịch."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xử lý webhook thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ hoặc chữ ký không hợp lệ")
    })
    public ResponseEntity<BaseResponse<Void>> handleWebhook(
            @Parameter(
                    description = "Payload webhook do PayOS gửi về",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PayOsWebhookRequest.class))
            )
            @Valid  @RequestBody PayOsWebhookRequest request
    ) {
        log.info("Received PayOS webhook request: {}", request);
        try {
            payOsWebhookService.handle(request);
            log.info("Successfully processed PayOS webhook for order code: {}", request.getData().getOrderCode());
        } catch (Exception e) {
            log.error("Error processing PayOS webhook for order code {}: {}", request.getData().getOrderCode(), e.getMessage(), e);
        }
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success("Xử lý webhook thành công.")
                );
    }

    @PostMapping("/api/v1/confirm-webhook")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @Operation(
            summary = "Xác nhận webhook PayOS",
            description = "API trung gian dùng để gọi PayOS confirm webhook URL. " +
                    "Chỉ tài khoản có quyền SYS_ADMIN mới được phép sử dụng."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác nhận webhook thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<BaseResponse<Void>> handleConfirmWebhook(
            @Parameter(
                    description = "Body chứa webhookUrl cần đăng ký với PayOS",
                    example = "{ \"webhookUrl\": \"https://example.com/webhook/payos\" }",
                    required = true
            )
            @Valid @RequestBody Map<String, Object> confirm
    ) {
        log.info("Attempting to confirm PayOS webhook URL with request: {}", confirm);
        payOsClient.confirmHookUrl(confirm);
        log.info("Successfully confirmed PayOS webhook URL.");
        return ResponseEntity.ok()
                .body(
                        BaseResponse.success("Xác nhận webhook thành công.")
                );
    }
}
