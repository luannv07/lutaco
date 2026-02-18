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
import vn.id.luannv.lutaco.dto.response.PayOSResponse;
import vn.id.luannv.lutaco.entity.PayOS;
import vn.id.luannv.lutaco.enumerate.PaymentStatus;
import vn.id.luannv.lutaco.repository.PayOSRepository;
import vn.id.luannv.lutaco.service.PayOsClient;

import java.time.LocalDateTime;
import java.util.List;

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

        List<PayOS> pendings =
                payOSRepository.findByStatusAndPaidAtIsNullAndCreatedDateIsLessThan(
                        PaymentStatus.PENDING,
                        LocalDateTime.now().minusSeconds(expirationTime)
                );

        log.debug("[system]: Found {} pending payments to reconcile.", pendings.size());

        for (PayOS payOS : pendings) {
            log.info("[system]: Reconciling payment for order code: {}", payOS.getOrderCode());
            PayOSResponse<PayOSResponse.PayOSDataDetail> detail =
                    payOsClient.getDetail(String.valueOf(payOS.getOrderCode()));

            if (detail == null) continue;
            if (detail.getData() == null) continue;

            PaymentStatus newStatus =
                    PaymentStatus.valueOf(detail.getData().getStatus());

            if (newStatus != payOS.getStatus()) {
                payOS.setStatus(newStatus);

                if (newStatus == PaymentStatus.PAID && payOS.getPaidAt() == null) {
                    payOS.setPaidAt(LocalDateTime.now());
                    log.info("[system]: Payment for order code {} is now PAID. Paid at: {}", payOS.getOrderCode(), payOS.getPaidAt());
                } else {
                    log.info("[system]: Payment for order code {} status updated from {} to {}.", payOS.getOrderCode(), payOS.getStatus(), newStatus);
                }
            } else {
                log.debug("[system]: Payment for order code {} status remains {}.", payOS.getOrderCode(), payOS.getStatus());
            }
        }

        log.info("[system]: Finished scheduled job: Reconciled {} pending payments.", pendings.size());
    }
}
