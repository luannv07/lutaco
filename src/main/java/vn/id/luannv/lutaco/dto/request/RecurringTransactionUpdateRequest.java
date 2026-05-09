package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionUpdateRequest {

    @Positive(message = "{validation.field.positive}")
    Long amount;

    @Size(max = 255, message = "{validation.field.too_long}")
    @Length(max = 255, message = "{validation.field.too_long}")
    String note;

    String frequentType;

    LocalDate endDate;

    LocalDate nextDate;
}
