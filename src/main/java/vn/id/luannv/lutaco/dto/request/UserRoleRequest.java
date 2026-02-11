package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserRoleRequest",
        description = "Request dùng để gán hoặc thay đổi vai trò (role) cho người dùng"
)
public class UserRoleRequest {

    @NotBlank(message = "{input.required}")
    @Schema(
            description = "Tên role cần gán cho người dùng",
            example = "ADMIN",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String roleName;
}
