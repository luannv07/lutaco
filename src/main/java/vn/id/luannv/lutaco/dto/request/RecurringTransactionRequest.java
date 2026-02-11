package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "RecurringTransactionRequest",
        description = "Request model for creating or updating a recurring transaction."
)
public class RecurringTransactionRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(
            description = "The ID of the base transaction to make recurring.",
            example = "txn_12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String transactionId;

    @NotBlank(message = "{validation.required}")
    @Schema(
            description = "The frequency of the recurrence.",
            example = "MONTHLY",
            allowableValues = {"DAILY", "WEEKLY", "MONTHLY", "YEARLY"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String frequentType;

    @NotNull(message = "{validation.required}")
    @FutureOrPresent(message = "{validation.field.future_or_present}")
    @Schema(
            description = "The start date for the recurring transaction.",
            example = "2024-07-01",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDate startDate;
}
