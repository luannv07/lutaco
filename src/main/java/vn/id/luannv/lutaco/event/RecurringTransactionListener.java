package vn.id.luannv.lutaco.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;
import vn.id.luannv.lutaco.service.AsyncEmailService;
import vn.id.luannv.lutaco.service.EmailTemplateService;
import vn.id.luannv.lutaco.service.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecurringTransactionListener {
    AsyncEmailService emailService;
    TransactionService transactionService;

    /**
     * Xử lý sự kiện tạo giao dịch định kỳ mới
     * Gửi email xác nhận cho người dùng
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void sendRecurringTransactionInitializationEmail(
            RecurringTransactionEvent.RecurringInitialization recurring) {
        try {
            log.info("[system]: Processing recurring transaction initialization event for transaction ID: {}",
                    recurring.getRecurringUserFields().getTransactionId());

            EmailTemplateService.EmailFields fields = EmailTemplateService.getRecurringInitializationTemplate(recurring);

            emailService.sendEmail(
                    fields.to(),
                    fields.subject(),
                    fields.body()
            );

            log.info("[system]: Successfully sent recurring transaction initialization email to: {}", fields.to());

        } catch (Exception e) {
            log.error("[system]: Error sending recurring transaction initialization email for transaction ID: {}", recurring.getRecurringUserFields().getTransactionId(), e);
        }
    }

    /**
     * Xử lý sự kiện giao dịch định kỳ được thực hiện
     * Gửi email thông báo cho người dùng về giao dịch đã xảy ra
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void sendRecurringTransactionFrequencyEmail(
            RecurringTransactionEvent.RecurringFrequency recurring) {
        transactionService
                .autoCreateTransactionWithCronJob(recurring.getRecurringUserFields().getTransactionId(), recurring.getRecurringUserFields().getUserId());
        try {
            log.info("[system]: Processing recurring transaction frequency event for transaction ID: {}",
                    recurring.getRecurringUserFields().getTransactionId());

            EmailTemplateService.EmailFields fields = EmailTemplateService.getRecurringFrequencyTemplate(recurring);

            // Gửi email
            emailService.sendEmail(
                    fields.to(),
                    fields.subject(),
                    fields.body()
            );

            log.info("[system]: Successfully sent recurring transaction frequency email to: {}", fields.to());

        } catch (Exception e) {
            log.error("[system]: Error sending recurring transaction frequency email for transaction ID: {}", recurring.getRecurringUserFields().getTransactionId(), e);
        }
    }


}
