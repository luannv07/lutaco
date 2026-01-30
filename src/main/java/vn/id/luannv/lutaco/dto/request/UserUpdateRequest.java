package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserDtoRequest",
        description = "Request dùng để tạo mới hoặc đăng ký người dùng"
)
public class UserUpdateRequest {

    @Size(min = 2, max = 255, message = "{input.invalid}")
    @Schema(
            description = "Họ và tên người dùng",
            example = "Nguyễn Văn Luận",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String fullName;

    @Size(max = 255, message = "{input.tooLong}")
    @Schema(
            description = "Địa chỉ người dùng",
            example = "Hà Nội",
            maxLength = 255
    )
    String address;

    @Schema(
            description = "Giới tính người dùng",
            example = "MALE",
            allowableValues = {"MALE", "FEMALE", "OTHER"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String gender;
}
