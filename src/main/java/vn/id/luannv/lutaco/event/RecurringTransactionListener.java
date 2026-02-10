package vn.id.luannv.lutaco.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.id.luannv.lutaco.enumerate.FrequentType;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;
import vn.id.luannv.lutaco.service.AsyncEmailService;
import vn.id.luannv.lutaco.service.EmailTemplateService;
import vn.id.luannv.lutaco.service.TransactionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            log.info("Processing RecurringInitialization event for transaction: {}",
                    recurring.getRecurringUserFields().getTransactionId());

            EmailTemplateService.EmailFields fields = EmailTemplateService.getRecurringInitializationTemplate(recurring);

            emailService.sendEmail(
                    fields.to(),
                    fields.subject(),
                    fields.body()
            );

            log.info("Successfully sent RecurringInitialization email to: {}", fields.to());

        } catch (Exception e) {
            log.error("Error sending RecurringInitialization email", e);
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
            log.info("Processing RecurringFrequency event for transaction: {}",
                    recurring.getRecurringUserFields().getTransactionId());

            EmailTemplateService.EmailFields fields = EmailTemplateService.getRecurringFrequencyTemplate(recurring);

            // Gửi email
            emailService.sendEmail(
                    fields.to(),
                    fields.subject(),
                    fields.body()
            );

            log.info("Successfully sent RecurringFrequency email to: {}", fields.to());

        } catch (Exception e) {
            log.error("Error sending RecurringFrequency email", e);
        }
    }


}