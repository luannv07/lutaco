package vn.id.luannv.lutaco.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "PayOSResponse",
        description = "Response chuẩn trả về từ PayOS"
)
public class PayOSResponse<T> {

    @Schema(
            description = "Mã trạng thái phản hồi từ PayOS",
            example = "00"
    )
    String code;

    @Schema(
            description = "Mô tả trạng thái phản hồi từ PayOS",
            example = "Success"
    )
    String desc;

    @Schema(
            description = "Dữ liệu chi tiết trả về từ PayOS (phụ thuộc từng API)"
    )
    T data;

    @Schema(
            description = "Chữ ký xác thực dữ liệu từ PayOS",
            example = "f3b9c2e1..."
    )
    String signature;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Schema(
            name = "PayOSDataCreated",
            description = "Dữ liệu trả về khi tạo payment link thành công"
    )
    public static class PayOSDataCreated {

        @Schema(example = "970418")
        String bin;

        @Schema(example = "123456789")
        String accountNumber;

        @Schema(example = "NGUYEN VAN A")
        String accountName;

        @Schema(example = "100000")
        Long amount;

        @Schema(example = "Thanh toán đơn hàng #123")
        String description;

        @Schema(example = "123456")
        Integer orderCode;

        @Schema(example = "VND")
        String currency;

        @Schema(example = "payos_abc123")
        String paymentLinkId;

        @Schema(
                description = "Trạng thái payment link",
                example = "PENDING"
        )
        String status;

        @Schema(
                description = "URL checkout để người dùng thực hiện thanh toán",
                example = "https://checkout.payos.vn/..."
        )
        String checkoutUrl;

        @Schema(
                description = "QR code thanh toán (dạng text/base64)",
                example = "000201010212..."
        )
        String qrCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Schema(
            name = "PayOSDataDetail",
            description = "Chi tiết trạng thái thanh toán của đơn hàng hoặc payment link"
    )
    public static class PayOSDataDetail {

        @Schema(
                description = "ID truy vấn (có thể là orderCode hoặc paymentLinkId)",
                example = "123456"
        )
        String id;

        @Schema(example = "123456")
        int orderCode;

        @Schema(example = "100000")
        int amount;

        @Schema(
                description = "Số tiền đã thanh toán",
                example = "50000"
        )
        int amountPaid;

        @Schema(
                description = "Số tiền còn lại cần thanh toán",
                example = "50000"
        )
        int amountRemaining;

        @Schema(
                description = "Trạng thái thanh toán",
                example = "PARTIAL"
        )
        String status;

        @Schema(
                description = "Thời điểm tạo payment link",
                example = "2024-01-01T10:00:00Z"
        )
        Instant createdAt;

        @Schema(
                description = "Danh sách giao dịch liên quan đến payment link"
        )
        List<Object> transactions;
    }
}
