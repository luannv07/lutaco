package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UpdatePasswordRequest",
        description = "Request model for updating a user's password."
)
public class UpdatePasswordRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(
            description = "The user's current password.",
            example = "oldPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String oldPassword;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
    @Schema(
            description = "The new password (at least 6 characters).",
            example = "newStrongPassword456",
            minLength = 6,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String newPassword;

    @NotBlank(message = "{validation.required}")
    @Schema(
            description = "Confirmation of the new password.",
            example = "newStrongPassword456",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
    String confirmPassword;
}
