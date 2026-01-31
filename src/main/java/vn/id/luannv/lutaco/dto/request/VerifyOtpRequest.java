package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.OtpType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "VerifyOtpRequest",
        description = "Request dùng để xác thực mã OTP của người dùng"
)
public class VerifyOtpRequest {

    @NotBlank(message = "{input.required}")
    @Email(message = "{input.invalid}")
    @Size(max = 255, message = "{input.tooLong}")
    @Schema(
            description = "Email đã dùng để đăng ký / nhận OTP",
            example = "vanluandvlp@gmail.com",
            format = "email",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email;

    @NotNull(message = "{input.required}")
    @Schema(
            description = "Loại OTP cần xác thực (đăng ký, quên mật khẩu, ...)",
            example = "REGISTER",
            allowableValues = {"REGISTER", "FORGOT_PASSWORD"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    OtpType otpType;

    @NotBlank(message = "{input.required}")
    @Size(min = 6, max = 6, message = "{input.invalid}")
    @Schema(
            description = "Mã OTP gồm 6 chữ số",
            example = "123456",
            minLength = 6,
            maxLength = 6,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String code;
}
