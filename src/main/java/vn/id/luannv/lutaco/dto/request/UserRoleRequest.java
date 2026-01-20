package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserRoleRequest",
        description = "Request dùng để gán hoặc thay đổi vai trò cho người dùng"
)
public class UserRoleRequest {

    @NotBlank(message = "{input.required}")
    @Schema(
            description = "Tên role cần gán cho user",
            example = "ADMIN",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String roleName;
}
