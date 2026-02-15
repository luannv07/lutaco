package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        description = "Request dùng để lọc và phân trang danh mục (Category)"
)
public class CategoryFilterRequest {

    @Schema(
            description = "Tên danh mục cần tìm kiếm (tìm gần đúng)",
            example = "Ăn uống"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryName;

    @Schema(
            description = "Loại danh mục (EXPENSE | INCOME)",
            example = "EXPENSE"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryType;
}
