package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String fullName;

    @Size(max = 255, message = "{validation.field.too_long}")
    String address;

    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String gender;
}
