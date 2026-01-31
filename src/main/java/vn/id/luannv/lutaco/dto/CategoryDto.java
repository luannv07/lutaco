package vn.id.luannv.lutaco.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {
    String categoryName;
    String parentId;
    CategoryType categoryType;
    List<CategoryDto> children;
    Boolean isSystem;
    LocalDateTime createdDate;
    String createdBy;
}
