package vn.id.luannv.lutaco.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.id.luannv.lutaco.event.entity.TransactionChangedEvent;
import vn.id.luannv.lutaco.service.BudgetService;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionChangedEventListener {

    BudgetService budgetService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TransactionChangedEvent event) {
        log.info("[transaction-event]: transaction {} changed, refreshing budgets for user={}, category={}",
                event.getTransactionId(), event.getUserId(), event.getCategoryId());
        budgetService.refreshBudgetsForTransaction(event.getUserId(), event.getCategoryId());
    }
}