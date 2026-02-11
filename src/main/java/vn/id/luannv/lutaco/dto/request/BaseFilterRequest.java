package vn.id.luannv.lutaco.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        description = "Request base dùng cho phân trang, được kế thừa bởi các filter request khác"
)
public class BaseFilterRequest {

    @Schema(
            description = "Số trang cần lấy (bắt đầu từ 1)",
            example = "1",
            minimum = "1"
    )
    @Range(min = 1, max = 1000, message = "{validation.field.size_not_in_range}")
    Integer page = 1;

    @Schema(
            description = "Số bản ghi trên mỗi trang (tối đa 100)",
            example = "10",
            minimum = "1",
            maximum = "100"
    )
    @Range(min = 1, max = 1000, message = "{validation.field.size_not_in_range}")
    Integer size = 10;
}
