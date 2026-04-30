package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePasswordRequest {

    @NotBlank(message = "{validation.required}")
        @Length(max = 255, message = "{validation.field.too_long}")
    String oldPassword;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
        String newPassword;

    @NotBlank(message = "{validation.required}")
        @Size(min = 6, max = 255, message = "{validation.field.size_not_in_range}")
    String confirmNewPassword;
}
