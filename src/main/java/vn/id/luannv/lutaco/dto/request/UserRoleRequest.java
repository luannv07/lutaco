package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRoleRequest {

    @NotBlank(message = "{validation.required}")
        @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String roleName;
}
