package vn.id.luannv.lutaco.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.event.entity.TransactionCreatedEvent;
import vn.id.luannv.lutaco.event.entity.TransactionDeletedEvent;
import vn.id.luannv.lutaco.repository.BudgetRepository;
import vn.id.luannv.lutaco.service.AsyncEmailService;
import vn.id.luannv.lutaco.service.EmailTemplateService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetUpdateListener {

    private final BudgetRepository budgetRepository;
    private final AsyncEmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        Transaction transaction = event.getTransaction();
        log.info("Handling transaction created event for transaction id: {}", transaction.getId());

        String userId = transaction.getUserId();
        String categoryId = transaction.getCategory().getId();
        LocalDate transactionDate = transaction.getTransactionDate().toLocalDate();

        Optional<Budget> budgetOpt = budgetRepository.findActiveBudget(userId, categoryId, transactionDate);

        if (budgetOpt.isEmpty()) {
            log.warn("No active budget found for user {} and category {} on date {}", userId, categoryId, transactionDate);
            return;
        }

        Budget budget = budgetOpt.get();
        long currentActualAmount = budget.getActualAmount();
        long transactionAmount = transaction.getAmount();

        long newActualAmount = currentActualAmount + transactionAmount;
        budget.setActualAmount(newActualAmount);

        if (budget.getTargetAmount() > 0) {
            float percentage = ((float) newActualAmount / budget.getTargetAmount()) * 100;
            budget.setPercentage(percentage);
            budget.setStatus(updateStatus(percentage));
        }

        if (budget.getStatus() == BudgetStatus.DANGER || budget.getStatus() == BudgetStatus.WARNING) {
            EmailTemplateService.EmailFields fields = EmailTemplateService.sendAttentionBudget(budget);
            log.info("Before sending email at {}", LocalDateTime.now());
            emailService.sendEmail(
                    fields.to(),
                    fields.subject(),
                    fields.body()
            );
        }

        budgetRepository.save(budget);
        log.info("Successfully updated budget {} with new actual amount {} and percentage {}",
                budget.getId(), newActualAmount, budget.getPercentage());
    }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTransactionDeletedEvent(TransactionDeletedEvent event) {
        log.info("Listener received TransactionDeletedEvent for transaction id: {}", event.getTransaction().getId());

        Transaction transaction = event.getTransaction();
        String userId = transaction.getUserId();
        String categoryId = transaction.getCategory().getId();
        LocalDate transactionDate = transaction.getTransactionDate().toLocalDate();

        Optional<Budget> budgetOpt = budgetRepository.findActiveBudget(userId, categoryId, transactionDate);

        if (budgetOpt.isEmpty()) {
            log.warn("No active budget found for user {} and category {} on date {}. Skipping budget update.", userId, categoryId, transactionDate);
            return;
        }

        Budget budget = budgetOpt.get();
        long currentActualAmount = budget.getActualAmount();
        long transactionAmount = transaction.getAmount();

        // Subtract the amount of the deleted transaction
        long newActualAmount = currentActualAmount - transactionAmount;
        budget.setActualAmount(newActualAmount);

        float newPercentage = 0.0f;
        if (budget.getTargetAmount() > 0) {
            newPercentage = ((float) newActualAmount / budget.getTargetAmount()) * 100;
        }
        budget.setPercentage(newPercentage);

        budgetRepository.save(budget);
        log.info("Successfully updated budget {} after transaction deletion. New actual amount: {}, New percentage: {}",
                budget.getId(), newActualAmount, newPercentage);
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
