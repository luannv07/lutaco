package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "LoginRequest",
        description = "Request model for user authentication."
)
public class LoginRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "The user's username or email address.",
            example = "luannv",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String username;

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "The user's password.",
            example = "luannv",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String password;
}
