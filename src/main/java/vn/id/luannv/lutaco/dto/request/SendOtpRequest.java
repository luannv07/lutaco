package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.id.luannv.lutaco.enumerate.OtpType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendOtpRequest {
    @NotBlank(message = "{input.required}")
    @Email(message = "{input.invalid}")
    @Size(max = 255, message = "{input.tooLong}")
    @Schema(
            description = "Email của người dùng",
            example = "vanluandvlp@gmail.com",
            format = "email",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email;

    @NotNull(message = "{input.required}")
    @Schema(
            example = "REGISTER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    OtpType otpType;
}
