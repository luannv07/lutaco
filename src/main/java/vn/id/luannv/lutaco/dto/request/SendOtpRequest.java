package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendOtpRequest {

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.invalid.email}")
    @Length(max = 1000, message = "{validation.field.too_long}")
        String email;

    @NotNull(message = "{validation.required}")
    @Length(max = 1000, message = "{validation.field.too_long}")
        String otpType;

    @NotNull(message = "{validation.required}")
    @Length(max = 50, message = "{validation.field.too_long}")
        String username;
}
