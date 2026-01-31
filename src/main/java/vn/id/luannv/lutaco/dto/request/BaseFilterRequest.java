package vn.id.luannv.lutaco.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

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
    @Min(value = 1, message = "{pagination.page.invalid}")
    Integer page = 1;

    @Schema(
            description = "Số bản ghi trên mỗi trang (tối đa 100)",
            example = "10",
            minimum = "1",
            maximum = "100"
    )
    @Min(value = 1, message = "{pagination.size.invalid}")
    @Max(value = 100, message = "{pagination.size.invalid}")
    Integer size = 10;
}
