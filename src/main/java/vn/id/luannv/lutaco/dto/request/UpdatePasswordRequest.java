package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePasswordRequest {
    @NotBlank(message = "{input.required}")
    String oldPassword;
    @NotBlank(message = "{input.required}")
    @Size(min = 6, max = 255, message = "{input.invalid}")
    String newPassword;
    @NotBlank(message = "{input.required}")
    @Size(min = 6, max = 255, message = "{input.invalid}")
    String confirmNewPassword;
}
