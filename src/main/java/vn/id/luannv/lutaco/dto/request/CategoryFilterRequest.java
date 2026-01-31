package vn.id.luannv.lutaco.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryFilterRequest extends BaseFilterRequest {
    @Length(max = 255, message = "{input.invalid}")
    String categoryName;
    @Length(max = 255, message = "{input.invalid}")
    String categoryType;
}
