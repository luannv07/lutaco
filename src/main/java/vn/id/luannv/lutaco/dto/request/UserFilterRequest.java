package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "UserFilterRequest",
        description = "Request dùng để lọc, tìm kiếm và phân trang danh sách người dùng"
)
public class UserFilterRequest extends BaseFilterRequest {

    @Schema(
            description = "Tên đăng nhập (tìm gần đúng, không phân biệt hoa thường)",
            example = "luannv"
    )
    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String username;


    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    @Schema(
            description = "Địa chỉ người dùng (tìm gần đúng)",
            example = "Hà Nội"
    )
    String address;

    @Schema(
            description = "Trạng thái người dùng",
            example = "ACTIVE"
    )
    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String userStatus;

    @Range(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    @Schema(
            description = "ID vai trò (role) của người dùng",
            example = "1",
            minimum = "1"
    )
    Integer roleId;

    @Schema(
            description = "Gói người dùng (FREE / PREMIUM)",
            example = "FREE"
    )
    @Size(min = 2, max = 255, message = "{validation.field.size_not_in_range}")
    String userPlan;
}
