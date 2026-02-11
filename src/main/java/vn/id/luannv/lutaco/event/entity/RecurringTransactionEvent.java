package vn.id.luannv.lutaco.event.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.enumerate.FrequentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
public class RecurringTransactionEvent {
    public enum RecurringTransactionState {
        INITIALIZER,
        FREQUENCY
    }
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RecurringUserFields {
        String transactionId;
        String note;
        String walletId;
        String walletName;
        String email;
        String fullName;
        Long amount;
        String userId;
        String categoryName;
        CategoryType categoryType;
    }

    @SuperBuilder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    protected abstract static class RecurringComposite {
        Long recurringTransactionId;
        LocalDate nextPaymentDate;
        FrequentType frequentType;
        RecurringUserFields recurringUserFields;
    }

    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RecurringInitialization extends RecurringComposite {
        LocalDate startDate;
        LocalDateTime createdDate;
    }

    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RecurringFrequency extends RecurringComposite {
    }
}
