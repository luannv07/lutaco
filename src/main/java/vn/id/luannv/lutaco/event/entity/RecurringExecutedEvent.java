package vn.id.luannv.lutaco.event.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringExecutedEvent {
    Long recurringJobId;
    Long generatedTransactionId;
    Long userId;
    String categoryType;
}
