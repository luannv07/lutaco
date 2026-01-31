package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "PayOsWebhookData",
        description = "Dữ liệu chi tiết PayOS gửi về thông qua webhook sau khi giao dịch chuyển khoản hoàn tất"
)
/*
 * Không cần validate request vì được build từ phía PayOS gửi về webhook
 */
public class PayOsWebhookData {

    @Schema(
            description = "Số tài khoản nhận tiền",
            example = "123456789"
    )
    String accountNumber;

    @Schema(
            description = "Số tiền giao dịch (VND)",
            example = "50000"
    )
    Integer amount;

    @Schema(
            description = "Nội dung chuyển khoản",
            example = "Thanh toan don hang 123456"
    )
    String description;

    @Schema(
            description = "Mã tham chiếu giao dịch từ ngân hàng",
            example = "FT123456789"
    )
    String reference;

    @Schema(
            description = "Thời điểm phát sinh giao dịch (ISO-8601)",
            example = "2025-01-31T10:15:30"
    )
    String transactionDateTime;

    @Schema(
            description = "Số tài khoản ảo (Virtual Account) dùng cho giao dịch",
            example = "970400123456789"
    )
    String virtualAccountNumber;

    @Schema(
            description = "Mã ngân hàng của tài khoản chuyển tiền",
            example = "VCB"
    )
    String counterAccountBankId;

    @Schema(
            description = "Tên ngân hàng của tài khoản chuyển tiền",
            example = "Vietcombank"
    )
    String counterAccountBankName;

    @Schema(
            description = "Tên chủ tài khoản chuyển tiền",
            example = "NGUYEN VAN A"
    )
    String counterAccountName;

    @Schema(
            description = "Số tài khoản chuyển tiền",
            example = "0123456789"
    )
    String counterAccountNumber;

    @Schema(
            description = "Tên tài khoản ảo",
            example = "LUTACO PREMIUM"
    )
    String virtualAccountName;

    @Schema(
            description = "Loại tiền tệ",
            example = "VND"
    )
    String currency;

    @Schema(
            description = "Mã đơn hàng nội bộ dùng để đối soát",
            example = "123456789"
    )
    Integer orderCode;

    @Schema(
            description = "ID của payment link trên PayOS",
            example = "PL_abc123xyz"
    )
    String paymentLinkId;

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
}
