package vn.id.luannv.lutaco.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "CategoryRequest",
        description = "Thông tin danh mục giao dịch, hỗ trợ cấu trúc cây (parent - children)"
)
public class CategoryRequest {

    @Schema(
            description = "Tên danh mục",
            example = "Ăn uống"
    )
    @NotBlank(message = "{validation.field.required}")
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryName;

    @Schema(
            description = "ID danh mục cha (null nếu là danh mục gốc)",
            example = "123"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String parentId;

    @Schema(
            description = "Loại danh mục"
    )
    String categoryType;
}
