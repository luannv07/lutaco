package vn.id.luannv.lutaco.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryRequest {

    @NotBlank(message = "{validation.field.required}")
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryName;

    @Length(max = 255, message = "{validation.field.too_long}")
    String parentId;

    String categoryType;
}
