package vn.id.luannv.lutaco.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseFilterRequest {

    @Range(min = 1, max = 1000, message = "{validation.field.size_not_in_range}")
    Integer page = 1;

    @Range(min = 1, max = 1000, message = "{validation.field.size_not_in_range}")
    Integer size = 10;
}
