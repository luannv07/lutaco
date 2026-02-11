package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserCreateRequest",
        description = "Request model for creating a new user account."
)
@Builder
public class UserCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
    @Schema(
            description = "The user's desired username.",
            example = "newuser",
            minLength = 6,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String username;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
    @Schema(
            description = "The user's password (at least 6 characters).",
            example = "strongpassword123",
            minLength = 6,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String password;

    @NotBlank(message = "{validation.required}")
    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    @Schema(
            description = "The user's full name.",
            example = "John Doe",
            minLength = 2,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String fullName;

    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "The user's address.",
            example = "123 Main St, Anytown, USA",
            maxLength = 255
    )
    String address;

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.invalid.email}")
    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "The user's email address (used for verification and notifications).",
            example = "john.doe@example.com",
            format = "email",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email;

    @NotBlank(message = "{validation.required}")
    @Schema(
            description = "The user's gender.",
            example = "MALE",
            allowableValues = {"MALE", "FEMALE", "OTHER"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String gender;
}
