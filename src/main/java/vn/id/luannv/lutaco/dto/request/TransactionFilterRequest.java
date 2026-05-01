package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionFilterRequest extends BaseFilterRequest {

    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryName;

    LocalDateTime fromDate;

    LocalDateTime toDate;

    Long minAmount;

    Long maxAmount;

    @Length(max = 255, message = "{validation.field.too_long}")
    String walletName;
}
