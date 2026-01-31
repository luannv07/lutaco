package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserCreateRequest",
        description = "Request dùng để đăng ký hoặc tạo mới người dùng trong hệ thống"
)
@Builder
public class UserCreateRequest {

    @NotBlank(message = "{input.required}")
    @Size(min = 6, max = 255, message = "{input.invalid}")
    @Schema(
            description = "Tên đăng nhập của người dùng",
            example = "{{account_test}}",
            minLength = 6,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String username;

    @NotBlank(message = "{input.required}")
    @Size(min = 6, max = 255, message = "{input.invalid}")
    @Schema(
            description = "Mật khẩu đăng nhập (tối thiểu 6 ký tự)",
            example = "{{account_test}}",
            minLength = 6,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String password;

    @NotBlank(message = "{input.required}")
    @Size(min = 2, max = 255, message = "{input.invalid}")
    @Schema(
            description = "Họ và tên đầy đủ của người dùng",
            example = "Nguyễn Văn Luận",
            minLength = 2,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String fullName;

    @Size(max = 255, message = "{input.tooLong}")
    @Schema(
            description = "Địa chỉ hiện tại của người dùng",
            example = "Hà Nội",
            maxLength = 255
    )
    String address;

    @NotBlank(message = "{input.required}")
    @Email(message = "{input.invalid}")
    @Size(max = 255, message = "{input.tooLong}")
    @Schema(
            description = "Email của người dùng (dùng để xác thực và nhận OTP)",
            example = "vanluandvlp@gmail.com",
            format = "email",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email;

    @NotBlank(message = "{input.required}")
    @Schema(
            description = "Giới tính người dùng",
            example = "MALE",
            allowableValues = {"MALE", "FEMALE", "OTHER"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String gender;
}
