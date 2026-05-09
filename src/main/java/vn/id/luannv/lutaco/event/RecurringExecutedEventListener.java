package vn.id.luannv.lutaco.event;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.event.entity.RecurringExecutedEvent;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecurringExecutedEventListener {

    @Async
    @EventListener
    public void handle(RecurringExecutedEvent event) {
        log.info("[recurring-event]: Job {} executed — generatedTxId={}, userId={}, categoryType={}",
                event.getRecurringJobId(),
                event.getGeneratedTransactionId(),
                event.getUserId(),
                event.getCategoryType());
    }
}
