package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
            description = "The new active status for the user.",
            example = "BANNED",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String status;
}
