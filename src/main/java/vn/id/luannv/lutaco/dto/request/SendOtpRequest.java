package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.id.luannv.lutaco.enumerate.OtpType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "SendOtpRequest",
        description = "Request model for sending an OTP to a user."
)
public class SendOtpRequest {

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.invalid.email}")
    @Length(max = 1000, message = "{validation.field.too_long}")
    @Schema(
            description = "The email address to send the OTP to.",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email;

    @NotNull(message = "{validation.required}")
    @Length(max = 1000, message = "{validation.field.too_long}")
    @Schema(
            description = "The type of OTP to be sent (e.g., REGISTRATION, PASSWORD_RESET).",
            example = "REGISTRATION",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String otpType;
}
