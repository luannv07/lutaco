package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "WalletCreateRequest",
        description = "Request model for creating a new wallet."
)
public class WalletCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "The name of the wallet.",
            example = "Personal Savings",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String walletName;

    @NotNull(message = "{validation.required}")
    @PositiveOrZero(message = "{validation.field.positive_or_zero}")
    @Schema(
            description = "The initial balance of the wallet.",
            example = "1000000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long balance;

    @Size(max = 500, message = "{validation.field.too_long}")
    @Schema(
            description = "A description for the wallet.",
            example = "Wallet for daily expenses and savings."
    )
    String description;
}
