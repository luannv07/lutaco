package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "RoleFilterRequest",
        description = "Request dùng để lọc và tìm kiếm Role"
)
public class RoleFilterRequest extends BaseFilterRequest {

    @Schema(
            description = "Tên role cần tìm kiếm (tìm gần đúng, không phân biệt hoa thường)",
            example = "ADMIN"
    )
    @Length(max = 255, message = "{input.invalid}")
    String name;
}
