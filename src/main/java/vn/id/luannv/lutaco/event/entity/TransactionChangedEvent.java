package vn.id.luannv.lutaco.event.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionChangedEvent {
    Long transactionId;
    Long userId;
    Long categoryId;
}