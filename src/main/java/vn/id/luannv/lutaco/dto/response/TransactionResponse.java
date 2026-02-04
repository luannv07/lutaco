package vn.id.luannv.lutaco.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.TransactionType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "TransactionResponse",
        description = "Thông tin giao dịch của người dùng"
)
public class TransactionResponse {

    @Schema(
            description = "ID của giao dịch",
            example = "tx_123456"
    )
    String id;

    @Schema(
            description = "ID danh mục của giao dịch",
            example = "cat_001"
    )
    String categoryId;

    @Schema(
            description = "Tên danh mục của giao dịch",
            example = "Ăn uống"
    )
    String categoryName;

    @Schema(
            description = "Số tiền giao dịch",
            example = "150000"
    )
    Long amount;

    @Schema(
            description = "Loại giao dịch",
            example = "EXPENSE"
    )
    TransactionType transactionType;

    @Schema(
            description = "Thời điểm phát sinh giao dịch",
            example = "2026-01-01T10:30:00"
    )
    LocalDateTime transactionDate;

    @Schema(
            description = "Ghi chú cho giao dịch",
            example = "Ăn trưa với khách hàng"
    )
    String note;

    @Schema(
            description = "Thời điểm tạo bản ghi giao dịch",
            example = "2026-01-01T10:31:00"
    )
    LocalDateTime createdDate;

    @Schema(
            description = "ID ví",
            example = "b794886d-a7eb-4d41-ae22-04891ed3b352"
    )
    String walletId;

    @Schema(
            description = "Tên ví",
            example = "Ví MB Bank"
    )
    String walletName;
}
