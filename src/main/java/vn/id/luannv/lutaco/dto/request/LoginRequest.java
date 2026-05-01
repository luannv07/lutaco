package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    String username;

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    String password;
}
