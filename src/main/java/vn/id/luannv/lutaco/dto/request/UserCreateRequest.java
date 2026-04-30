package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
        String username;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
        String password;

    @NotBlank(message = "{validation.required}")
    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
        String fullName;

    @Size(max = 255, message = "{validation.field.too_long}")
        String address;

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.invalid.email}")
    @Size(max = 255, message = "{validation.field.too_long}")
        String email;

    @NotBlank(message = "{validation.required}")
        String gender;
}
