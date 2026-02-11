package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserStatusSetRequest",
        description = "Request model for setting a user's active status."
)
public class UserStatusSetRequest {

    @NotNull(message = "{validation.required}")
    @Schema(
            description = "The new active status for the user (true for active, false for inactive).",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Boolean isActive;
}
