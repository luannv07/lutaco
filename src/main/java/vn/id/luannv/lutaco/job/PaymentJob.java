package vn.id.luannv.lutaco.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.repository.PayOSRepository;
import vn.id.luannv.lutaco.service.PayOsClient;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentJob {
    PayOSRepository payOSRepository;
    PayOsClient payOsClient;
    @NonFinal
    @Value("${payment.expiration-time}")
    long expirationTime;

    @Scheduled(cron = "0 */30 * * * ?") // mỗi 30 phút, phút 0 và 30
    @Transactional
    public void reconcilePendingPayments() {

        log.info("[system]: Starting scheduled job: Reconciling pending payments.");
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
