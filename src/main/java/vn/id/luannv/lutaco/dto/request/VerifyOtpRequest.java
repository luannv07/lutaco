package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "VerifyOtpRequest",
        description = "Request dùng để xác thực mã OTP của người dùng"
)
public class VerifyOtpRequest {

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.failed}")
    @Size(max = 255, message = "{validation.field.too_long}")
    @Schema(
            description = "Email đã dùng để đăng ký / nhận OTP",
            example = "vanluandvlp@gmail.com",
            format = "email",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email;

    @NotNull(message = "{validation.required}")
    @Schema(
            description = "Loại OTP cần xác thực (đăng ký, quên mật khẩu, ...)",
            example = "REGISTER",
            allowableValues = {"REGISTER", "FORGOT_PASSWORD"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String otpType;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 6, message = "{validation.failed}")
    @Schema(
            description = "Mã OTP gồm 6 chữ số",
            example = "123456",
            minLength = 6,
            maxLength = 6,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String code;
}
