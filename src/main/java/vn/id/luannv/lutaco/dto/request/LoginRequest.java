package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.id.luannv.lutaco.annotation.bind.AuditUsername;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "LoginRequest",
        description = "Request dùng để xác thực người dùng khi đăng nhập hệ thống"
)
public class LoginRequest {

    @Schema(
            description = "Tên đăng nhập của người dùng (username hoặc email tuỳ cấu hình hệ thống)",
            example = "{{account_test}}",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Length(max = 255, message = "{input.invalid}")
    @AuditUsername
    String username;

    @Schema(
            description = "Mật khẩu đăng nhập tương ứng với username",
            example = "{{account_test}}",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Length(max = 255, message = "{input.invalid}")
    String password;
}
