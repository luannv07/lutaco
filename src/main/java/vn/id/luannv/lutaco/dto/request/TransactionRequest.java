package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "TransactionRequest",
        description = "Request tạo hoặc cập nhật giao dịch"
)
public class TransactionRequest {

    @NotBlank(message = "{input.required}")
    @Schema(
            description = "ID danh mục giao dịch",
            example = "FOOD",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String categoryId;

    @NotNull(message = "{input.required}")
    @Positive(message = "{input.invalid}")
    @Schema(
            description = "Số tiền giao dịch",
            example = "50000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long amount;

    @NotNull(message = "{input.required}")
    @Schema(
            description = "Loại giao dịch (INCOME / EXPENSE)",
            example = "EXPENSE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String transactionType;

    @NotNull(message = "{input.required}")
    @Schema(
            description = "Thời điểm phát sinh giao dịch",
            example = "2024-01-15T10:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDateTime transactionDate;

    @Schema(
            description = "Ghi chú giao dịch",
            example = "Ăn trưa cùng bạn bè"
    )
    String note;
}
