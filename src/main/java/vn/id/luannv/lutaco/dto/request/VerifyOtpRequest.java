package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyOtpRequest {

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.failed}")
    @Size(max = 255, message = "{validation.field.too_long}")
        String email;

    @NotNull(message = "{validation.required}")
        String otpType;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 6, message = "{validation.failed}")
        String code;
}
