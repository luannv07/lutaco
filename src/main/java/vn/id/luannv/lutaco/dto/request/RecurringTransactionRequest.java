package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransactionRequest {

    @NotNull(message = "{input.required}")
    String transactionId;

    @NotNull(message = "{input.required}")
    LocalDate startDate;

    @NotNull(message = "{input.required}")
    String frequentType;
}
