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

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    @Transactional
    public void reconcilePendingPayments() {

        log.info("⏰ Start reconcile pending payment");

        List<PayOS> pendings =
                payOSRepository.findByStatusAndPaidAtIsNullAndCreatedDateIsLessThan(
                        PaymentStatus.PENDING,
                        LocalDateTime.now().minusSeconds(expirationTime)
                );

        for (PayOS payOS : pendings) {
            PayOSResponse<PayOSResponse.PayOSDataDetail> detail =
                    payOsClient.getDetail(String.valueOf(payOS.getOrderCode()));

            PaymentStatus newStatus =
                    PaymentStatus.valueOf(detail.getData().getStatus());

            if (newStatus != payOS.getStatus()) {
                payOS.setStatus(newStatus);

                if (newStatus == PaymentStatus.PAID && payOS.getPaidAt() == null) {
                    payOS.setPaidAt(LocalDateTime.now());
                }
            }
        }

        log.info("✅ Finished reconcile pending payment, size={}", pendings.size());
    }


}
