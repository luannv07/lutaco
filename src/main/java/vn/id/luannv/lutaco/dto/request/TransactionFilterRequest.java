package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionFilterRequest extends BaseFilterRequest {

    Long categoryId;

    Instant fromDate;

    Instant toDate;

    Long minAmount;

    Long maxAmount;

    Long walletId;

}
