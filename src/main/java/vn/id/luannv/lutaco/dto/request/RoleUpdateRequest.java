package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "RoleUpdateRequest",
        description = "Request dùng để cập nhật thông tin Role"
)
public class RoleUpdateRequest {

    @Schema(
            description = "Mô tả vai trò",
            example = "Quyền quản trị hệ thống"
    )
    @Length(max = 255, message = "{input.invalid}")
    String description;
}
