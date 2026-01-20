package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatusSetRequest {
    @NotNull(message = "{input.required}")
    Boolean isActive;
}
