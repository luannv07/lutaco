package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusSetRequest {

    @NotNull(message = "{validation.required}")
    String status;
}
