package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.UserStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserFilterRequest",
        description = "Request dùng để lọc và tìm kiếm người dùng"
)
public class UserFilterRequest extends BaseFilterRequest {

    @Size(max = 50, message = "Username không được vượt quá 100 ký tự")
    @Schema(
            description = "Tên đăng nhập (tìm gần đúng, không phân biệt hoa thường)",
            example = "luannv"
    )
    String username;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    @Schema(
            description = "Địa chỉ người dùng (tìm gần đúng)",
            example = "Hà Nội"
    )
    String address;

    String userStatus;

    @Min(value = 1, message = "RoleId phải lớn hơn 0")
    @Schema(
            description = "ID của role",
            example = "1",
            minimum = "1"
    )
    Integer roleId;
}
