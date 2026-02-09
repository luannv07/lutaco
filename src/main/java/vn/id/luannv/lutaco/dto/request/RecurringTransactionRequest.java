package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionRequest {

    @NotNull(message = "{input.required}")
    String transactionId;

    @NotNull(message = "{input.required}")
    LocalDate startDate;

    @NotNull(message = "{input.required}")
    String frequentType;
}
