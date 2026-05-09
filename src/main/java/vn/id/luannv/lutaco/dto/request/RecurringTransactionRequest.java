package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionRequest {

    @NotNull(message = "{validation.required}")
    @Positive(message = "{validation.field.positive}")
    Long categoryId;

    @NotNull(message = "{validation.required}")
    @Positive(message = "{validation.field.positive}")
    Long walletId;

    @NotNull(message = "{validation.required}")
    @Positive(message = "{validation.field.positive}")
    Long amount;

    @Size(max = 255, message = "{validation.field.too_long}")
    @Length(max = 255, message = "{validation.field.too_long}")
    String note;

    @NotBlank(message = "{validation.required}")
    String frequentType;

    @NotNull(message = "{validation.required}")
    LocalDate startDate;

    LocalDate endDate;
}
