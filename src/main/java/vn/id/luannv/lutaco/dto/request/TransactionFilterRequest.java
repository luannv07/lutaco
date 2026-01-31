package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionFilterRequest extends BaseFilterRequest {
    String categoryId;
    String transactionType;
    LocalDateTime fromDate;
    LocalDateTime toDate;
    Long minAmount;
    Long maxAmount;
}
