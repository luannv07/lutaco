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

        // paymentLinkId thêm tiền tố "not_" để chỉ rằng cái link đó ko khả dụng, thay bằng uuid random,
        // nếu muốn xử lí lại "not_" thì phải xử lí lại logic phía dưới cùng của hàm
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

        PayOSResponse<PayOSResponse.PayOSDataCreated> response = webClient.post()
                .uri("/v2/payment-requests")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[PAYOS] create payment failed: {}", body);
                                    return Mono.error(() -> new BusinessException(ErrorCode.PAYMENT_PROVIDER_ERROR));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<PayOSResponse<PayOSResponse.PayOSDataCreated>>() {
                })
                .block();

        if (response != null) {
            // lỗi (chỉ trả mã lỗi + mô tả)
            if (response.getData() == null || response.getSignature() == null) {
                PayOSResponse<PayOSResponse.PayOSDataDetail> detail =
                        getDetailExecute(String.valueOf(payOS.getOrderCode()));

                try {
                    payOS.setStatus(PaymentStatus.valueOf(detail.getData().getStatus()));
                    if (payOS.getStatus() == PaymentStatus.PAID && payOS.getPaidAt() == null)
                        payOS.setPaidAt(LocalDateTime.now());
                } catch (Exception exception) {
                    payOS.setStatus(PaymentStatus.UNKNOWN);
                }
                payOSRepository.save(payOS);
                // 231: mã đơn hàng đã tồn tại; cập nhật lại db những mã đơn hàng trên payos đã có, còn lại ko lưu
                if (response.getCode() != null && response.getCode().equals("231")) {
                    log.info("local server chưa có đơn, trên api đã có!!");
                    throw new BusinessException(ErrorCode.PAYMENT_SYSTEM_ERROR);
                }
            } else {
                payOS.setStatus(PaymentStatus.PENDING);
                payOS.setPaymentLinkId(response.getData().getPaymentLinkId());
            }
        } else {
            // khi mà response đã ko có thì tức là phía payos bị lỗi
            throw new BusinessException(ErrorCode.PAYMENT_PROVIDER_ERROR);
        }

        if (payOS.getPaymentLinkId().contains("not_")) {
            log.info("PayOsClient createPayment response { [code : desc] : [{} : {}] } ", response.getCode(), response.getDesc());
            log.info("PayosClient Description length response: [{}: {}]", payOS.getDescription(),
                    payOS.getDescription().length());
            throw new BusinessException(ErrorCode.PAYMENT_SYSTEM_ERROR);
        }
        payOSRepository.save(payOS);
        return response;
    }

    public PayOSResponse<PayOSResponse.PayOSDataDetail> getDetail(String id) {
        return getDetailExecute(id);
    }

    public void confirmHookUrl(Map<String, Object> hookLink) {
        hookLink.putIfAbsent("webhookUrl", "");

        webClient.post()
                .uri("confirm-webhook")
                .bodyValue(hookLink)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    log.error("[PAYOS] confirm-webhook response error [body]: {}", body);
                                    return Mono.error(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, body));
                                })
                )
                .bodyToMono(Object.class)
                .block();
        log.info("[PAYOS] confirm-webhook response successfully");
    }

    // get chi tiết trạng thái order cho đơn hàng nào đó theo orderCode
    private PayOSResponse<PayOSResponse.PayOSDataDetail> getDetailExecute(String id) {
        return webClient.get()
                .uri("/v2/payment-requests/" + id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[PAYOS] getDetail failed: {}", body);
                                    return Mono.error(() -> new BusinessException(ErrorCode.PAYMENT_PROVIDER_ERROR));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<PayOSResponse<PayOSResponse.PayOSDataDetail>>() {
                })
                .block();
    }
}

