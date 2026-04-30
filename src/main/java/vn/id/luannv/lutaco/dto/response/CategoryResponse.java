package vn.id.luannv.lutaco.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.dto.EnumDisplay;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

        String id;

        String categoryName;

        String parentId;

        EnumDisplay<CategoryType> categoryType;

        List<CategoryResponse> children;

        Boolean isSystem;

        LocalDateTime createdDate;

        String createdBy;
    Boolean hasChildren = Boolean.FALSE;
}
