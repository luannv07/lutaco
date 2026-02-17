package vn.id.luannv.lutaco.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vn.id.luannv.lutaco.dto.request.PayOSRequest;
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.entity.PayOS;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.PaymentStatus;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.PayOSRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.util.PayOsSignatureUtils;
import vn.id.luannv.lutaco.util.RandomUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PayOsClient {

    @Qualifier("payOsWebClient")
    private final WebClient webClient;

    private final PayOSRepository payOSRepository;
    private final UserRepository userRepository;

    @Value("${payment.check-sum-key}")
    private String checkSumKey;

    @Value("${payment.expiration-time}")
    private long expirationTime;

    @Value("${payment.amount}")
    private int amount;

    @Value("${payment.url.cancel}")
    private String cancelUrl;

    @Value("${payment.url.result}")
    private String resultUrl;

    public PayOsClient(@Qualifier("payOsWebClient") WebClient webClient,
                       PayOSRepository payOSRepository,
                       UserRepository userRepository) {
        this.webClient = webClient;
        this.payOSRepository = payOSRepository;
        this.userRepository = userRepository;
    }

    @Transactional(dontRollbackOn = BusinessException.class)
    public PayOSResponse<PayOSResponse.PayOSDataCreated> createPayment(PaymentType paymentType) {
        Integer latestOrderCode = payOSRepository.findFirstByOrderByOrderCodeDesc()
                .map(PayOS::getOrderCode)
                .orElse(0);
        User currentUser = userRepository.findByUsername(SecurityUtils.getCurrentUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        String desc = String.format("premium plan %s", RandomUtils.randomAlphaNum(10));

        PayOS payOS = PayOS.builder()
                .orderCode(latestOrderCode + 1)
                .description(desc)
                .type(paymentType)
                .user(currentUser)
                .amount(amount)
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .paymentLinkId("not_" + UUID.randomUUID().toString().replace("-", ""))
                .build();

        PayOSRequest request = PayOSRequest.builder()
                .orderCode(payOS.getOrderCode())
                .description(payOS.getDescription())
                .amount(payOS.getAmount())
                .cancelUrl(cancelUrl)
                .returnUrl(resultUrl)
                .expiredAt(Math.toIntExact(Instant.now().plusSeconds(expirationTime).getEpochSecond()))
                .build();
        String signature = PayOsSignatureUtils.generateHmacSha256ForCreatePaymentRequest(request, checkSumKey);

        request.setSignature(signature);

        log.info("[system]: Sending payment creation request to PayOS for order code: {}", payOS.getOrderCode());
        PayOSResponse<PayOSResponse.PayOSDataCreated> response = webClient.post()
                .uri("/v2/payment-requests")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[system]: PayOS create payment failed for order code {}. Response body: {}", payOS.getOrderCode(), body);
                                    return Mono.error(() -> new BusinessException(ErrorCode.PAYMENT_PROVIDER_ERROR));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<PayOSResponse<PayOSResponse.PayOSDataCreated>>() {
                })
                .block();

        if (response != null) {
            if (response.getData() == null || response.getSignature() == null) {
                log.warn("[system]: PayOS response for order code {} indicates an error or missing data. Code: {}, Desc: {}", payOS.getOrderCode(), response.getCode(), response.getDesc());
                PayOSResponse<PayOSResponse.PayOSDataDetail> detail =
                        getDetailExecute(String.valueOf(payOS.getOrderCode()));

                try {
                    payOS.setStatus(PaymentStatus.valueOf(detail.getData().getStatus()));
                    if (payOS.getStatus() == PaymentStatus.PAID && payOS.getPaidAt() == null)
                        payOS.setPaidAt(LocalDateTime.now());
                } catch (Exception exception) {
                    log.error("[system]: Failed to parse payment status from PayOS detail response for order code {}. Status: {}", payOS.getOrderCode(), detail.getData().getStatus(), exception);
                    payOS.setStatus(PaymentStatus.UNKNOWN);
                }
                payOSRepository.save(payOS);
                if (response.getCode() != null && response.getCode().equals("231")) {
                    log.info("[system]: PayOS reported order code {} already exists. Local server did not have it, throwing PAYMENT_SYSTEM_ERROR.", payOS.getOrderCode());
                    throw new BusinessException(ErrorCode.PAYMENT_SYSTEM_ERROR);
                }
            } else {
                payOS.setStatus(PaymentStatus.PENDING);
                payOS.setPaymentLinkId(response.getData().getPaymentLinkId());
                log.info("[system]: PayOS payment link created successfully for order code {}. Payment Link ID: {}", payOS.getOrderCode(), payOS.getPaymentLinkId());
            }
        } else {
            log.error("[system]: Received null response from PayOS for order code {}.", payOS.getOrderCode());
            throw new BusinessException(ErrorCode.PAYMENT_PROVIDER_ERROR);
        }

        if (payOS.getPaymentLinkId().contains("not_")) {
            log.error("[system]: PayOS payment link ID for order code {} contains 'not_' prefix, indicating an issue. Response Code: {}, Description: {}. PayOS Description Length: {}", payOS.getOrderCode(), response.getCode(), response.getDesc(), payOS.getDescription().length());
            throw new BusinessException(ErrorCode.PAYMENT_SYSTEM_ERROR);
        }
        payOSRepository.save(payOS);
        return response;
    }

    public PayOSResponse<PayOSResponse.PayOSDataDetail> getDetail(String id) {
        log.info("[system]: Fetching payment details from PayOS for ID: {}", id);
        return getDetailExecute(id);
    }

    public void confirmHookUrl(Map<String, Object> hookLink) {
        hookLink.putIfAbsent("webhookUrl", "");
        log.info("[system]: Attempting to confirm webhook URL with PayOS: {}", hookLink.get("webhookUrl"));

        webClient.post()
                .uri("confirm-webhook")
                .bodyValue(hookLink)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    log.error("[system]: PayOS confirm-webhook request failed. Response body: {}", body);
                                    return Mono.error(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, body));
                                })
                )
                .bodyToMono(Object.class)
                .block();
        log.info("[system]: Successfully confirmed webhook URL with PayOS.");
    }

    // get chi tiết trạng thái order cho đơn hàng nào đó theo orderCode
    private PayOSResponse<PayOSResponse.PayOSDataDetail> getDetailExecute(String id) {
        log.debug("[system]: Executing PayOS getDetail for ID: {}", id);
        return webClient.get()
                .uri("/v2/payment-requests/" + id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[system]: PayOS getDetail failed for ID {}. Response body: {}", id, body);
                                    return Mono.error(() -> new BusinessException(ErrorCode.PAYMENT_PROVIDER_ERROR));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<PayOSResponse<PayOSResponse.PayOSDataDetail>>() {
                })
                .block();
    }
}
