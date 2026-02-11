package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "TransactionFilterRequest",
        description = "Request dùng để lọc và tìm kiếm danh sách giao dịch"
)
public class TransactionFilterRequest extends BaseFilterRequest {

    @Schema(
            description = "Tên danh mục giao dịch",
            example = "FOOD"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String categoryName;

    @Schema(
            description = "Thời gian bắt đầu (lọc từ ngày)",
            example = "2024-01-01T00:00:00"
    )

    LocalDateTime fromDate;

    @Schema(
            description = "Thời gian kết thúc (lọc đến ngày)",
            example = "2024-01-31T23:59:59"
    )
    @FutureOrPresent(message = "{validation.field.future_or_present}")
    LocalDateTime toDate;

    @Schema(
            description = "Số tiền tối thiểu",
            example = "10000"
    )
    Long minAmount;

    @Schema(
            description = "Số tiền tối đa",
            example = "1000000"
    )
    Long maxAmount;

    @Schema(
            description = "Tên ví",
            example = "b794886d-a7eb-4d41-ae22-04891ed3b3"
    )
    @Length(max = 255, message = "{validation.field.too_long}")
    String walletName;
}
