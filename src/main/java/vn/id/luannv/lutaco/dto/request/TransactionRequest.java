package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "TransactionRequest",
        description = "Request model for creating or updating a transaction."
)
public class TransactionRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(
            description = "The ID of the category for this transaction.",
            example = "cat_12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryId;

    @NotNull(message = "{validation.required}")
    @Positive(message = "{validation.field.positive}")
    @Schema(
            description = "The amount of the transaction.",
            example = "50000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long amount;

    @NotNull(message = "{validation.required}")
    @Schema(
            description = "The date and time the transaction occurred.",
            example = "2024-01-15T10:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @FutureOrPresent(message = "{validation.field.future_or_present}")
    LocalDateTime transactionDate;

    @NotBlank(message = "{validation.required}")
    @Schema(
            description = "The ID of the wallet for this transaction.",
            example = "wal_67890",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String walletId;

    @Size(max = 500, message = "{validation.field.too_long}")
    @Schema(
            description = "A note for the transaction.",
            example = "Lunch with colleagues"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String note;
}
