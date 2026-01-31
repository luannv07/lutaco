package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserStatusSetRequest",
        description = "Request dùng để cập nhật trạng thái hoạt động của người dùng"
)
public class UserStatusSetRequest {

    @NotNull(message = "{input.required}")
    @Schema(
            description = "Trạng thái hoạt động của người dùng (true: ACTIVE, false: INACTIVE)",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Boolean isActive;
}
