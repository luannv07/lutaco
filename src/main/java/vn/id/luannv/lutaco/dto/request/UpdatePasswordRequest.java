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
        name = "UpdatePasswordRequest",
        description = "Request dùng để cập nhật mật khẩu người dùng"
)
public class UpdatePasswordRequest {

    @NotBlank(message = "{input.required}")
    @Schema(
            description = "Mật khẩu hiện tại",
            example = "oldPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String oldPassword;

    @NotBlank(message = "{input.required}")
    @Size(min = 6, max = 255, message = "{input.invalid}")
    @Schema(
            description = "Mật khẩu mới",
            example = "newPassword123",
            minLength = 6,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String newPassword;

    @NotBlank(message = "{input.required}")
    @Size(min = 6, max = 255, message = "{input.invalid}")
    @Schema(
            description = "Xác nhận lại mật khẩu mới (phải trùng với mật khẩu mới)",
            example = "newPassword123",
            minLength = 6,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String confirmNewPassword;
}
