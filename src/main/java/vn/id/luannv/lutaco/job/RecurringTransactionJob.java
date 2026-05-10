package vn.id.luannv.lutaco.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.repository.RecurringTransactionRepository;
import vn.id.luannv.lutaco.service.RecurringTransactionService;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecurringTransactionJob {

    RecurringTransactionRepository recurringTransactionRepository;
    RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 * * * ?") // every hour at minute 0
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<Long> dueJobIds = recurringTransactionRepository.findDueActiveJobIds(today);

        if (dueJobIds.isEmpty()) {
            log.debug("[recurring-job]: No due recurring jobs for {}.", today);
            return;
        }

        log.info("[recurring-job]: Processing {} due recurring jobs for {}.", dueJobIds.size(), today);
        int success = 0;
        int failed = 0;

        for (Long jobId : dueJobIds) {
            try {
                recurringTransactionService.executeJobById(jobId);
                success++;
            } catch (Exception e) {
                failed++;
                log.error("[recurring-job]: Failed to execute job {}: {}", jobId, e.getMessage(), e);
            }
        }

        log.info("[recurring-job]: Batch complete. success={}, failed={}, total={}.",
                success, failed, dueJobIds.size());
    }
}
