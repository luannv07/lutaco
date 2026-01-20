package vn.id.luannv.lutaco.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseFilterRequest {

    @Min(value = 1, message = "{pagination.page.invalid}")
    Integer page = 1;

    @Min(value = 1, message = "{pagination.size.invalid}")
    @Max(value = 100, message = "{pagination.size.invalid}")
    Integer size = 10;
}

