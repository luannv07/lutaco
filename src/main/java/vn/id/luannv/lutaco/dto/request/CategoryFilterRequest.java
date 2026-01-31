package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        description = "Request dùng để lọc và phân trang danh mục (Category)"
)
public class CategoryFilterRequest extends BaseFilterRequest {

    @Schema(
            description = "Tên danh mục cần tìm kiếm (tìm gần đúng)",
            example = "Ăn uống"
    )
    @Length(max = 255, message = "{input.invalid}")
    String categoryName;

    @Schema(
            description = "Loại danh mục (EXPENSE | INCOME)",
            example = "EXPENSE"
    )
    @Length(max = 255, message = "{input.invalid}")
    String categoryType;
}
