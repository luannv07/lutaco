package vn.id.luannv.lutaco.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.event.entity.TransactionCreatedEvent;
import vn.id.luannv.lutaco.event.entity.TransactionDeletedEvent;
import vn.id.luannv.lutaco.repository.BudgetRepository;
import vn.id.luannv.lutaco.service.AsyncEmailService;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetUpdateListener {

    private final BudgetRepository budgetRepository;
    private final AsyncEmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {

    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTransactionDeletedEvent(TransactionDeletedEvent event) {
    }

    private BudgetStatus updateStatus(float percentage) {
        if (percentage > BudgetStatus.DANGER.getPercentage())
            return BudgetStatus.DANGER;
        if (percentage > BudgetStatus.WARNING.getPercentage())
            return BudgetStatus.WARNING;
        if (percentage > BudgetStatus.UNKNOWN.getPercentage())
            return BudgetStatus.NORMAL;
        return BudgetStatus.UNKNOWN;
    }
}
