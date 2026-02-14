package vn.id.luannv.lutaco.event.entity;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import vn.id.luannv.lutaco.entity.Transaction;

@Getter
public class TransactionDeletedEvent extends ApplicationEvent {

    private final Transaction transaction;

    public TransactionDeletedEvent(Object source, Transaction transaction) {
        super(source);
        this.transaction = transaction;
    }
}
