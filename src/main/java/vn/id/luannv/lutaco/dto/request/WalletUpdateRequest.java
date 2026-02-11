package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "WalletUpdateRequest",
        description = "Request model for updating an existing wallet."
)
public class WalletUpdateRequest {

    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "The new name for the wallet.",
            example = "My Updated Savings",
            maxLength = 255
    )
    String walletName;

    @Size(max = 500, message = "{validation.field.too_long}")
    @Schema(
            description = "The new description for the wallet.",
            example = "Updated description for daily expenses and savings."
    )
    String description;
}
