package vn.id.luannv.lutaco.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
