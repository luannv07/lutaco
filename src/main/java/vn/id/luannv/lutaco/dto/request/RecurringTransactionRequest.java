package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RecurringTransactionRequest {

    @NotBlank(message = "{validation.required}")
    String transactionId;

    @NotNull(message = "{validation.required}")
    String frequentType;

    @NotNull(message = "{validation.required}")
    @FutureOrPresent(message = "{validation.field.future_or_present}")
    LocalDate startDate;
}
