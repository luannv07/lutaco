package vn.id.luannv.lutaco.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.entity.RecurringTransaction;
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

    @Scheduled(cron = "0 5 0 * * ?") // 00:05 mỗi ngày
//    @Scheduled(fixedDelay = 10000) // 00:05 mỗi ngày
    public void processRecurringTransactions() {
        log.info("⏰ Starting recurring transaction job at {}", LocalDate.now());
        List<RecurringTransaction> transactions = recurringTransactionRepository.findAllByNextDateBefore(LocalDate.now().plusDays(1));
        int count = 0;

        for (RecurringTransaction rt : transactions) {
            try {
                recurringTransactionService.processOne(rt);
                ++count;
            } catch (Exception e) {
                log.error("Error processing recurring transaction with id: {}. This transaction will be rolled back.", rt.getId(), e);
            }
        }
        log.info("✅ Finished recurring transaction, count: {}", count);
    }
}
