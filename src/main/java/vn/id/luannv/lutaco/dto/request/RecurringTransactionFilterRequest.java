package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "RecurringTransactionFilterRequest", description = "Request for filtering recurring transactions")
public class RecurringTransactionFilterRequest extends BaseFilterRequest {

    @Schema(description = "Frequency of the recurrence", example = "MONTHLY")
    @Length(max = 255, message = "{validation.field.too_long}")
    String frequentType;
}
