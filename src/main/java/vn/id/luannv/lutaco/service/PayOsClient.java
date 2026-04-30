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
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.enumerate.PaymentType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.PayOSRepository;
import vn.id.luannv.lutaco.repository.UserRepository;

import java.util.Map;

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
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
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
