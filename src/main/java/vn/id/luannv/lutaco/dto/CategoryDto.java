package vn.id.luannv.lutaco.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "CategoryDto",
        description = "Thông tin danh mục giao dịch, hỗ trợ cấu trúc cây (parent - children)"
)
public class CategoryDto {

    @Schema(
            description = "ID Danh mục",
            example = "7a65ebc9-6b23-415d-922b-9722cd94b0ec"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String id;

    @Schema(
            description = "Tên danh mục",
            example = "Ăn uống"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryName;

    @Schema(
            description = "ID danh mục cha (null nếu là danh mục gốc)",
            example = "123"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String parentId;

    @Schema(
            description = "Loại danh mục",
            example = "EXPENSE"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    CategoryType categoryType;

    @Schema(
            description = "Danh sách danh mục con"
    )
    List<CategoryDto> children;

    @Schema(
            description = "Danh mục hệ thống hay do người dùng tạo",
            example = "true"
    )
    Boolean isSystem;

    @Schema(
            description = "Thời điểm tạo danh mục",
            example = "2024-01-01T10:00:00"
    )
    LocalDateTime createdDate;

    @Schema(
            description = "Người tạo danh mục",
            example = "system"
    )
    String createdBy;
}
