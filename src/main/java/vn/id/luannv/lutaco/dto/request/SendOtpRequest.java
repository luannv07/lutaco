package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendOtpRequest {
    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.failed}")
    String email;
}
