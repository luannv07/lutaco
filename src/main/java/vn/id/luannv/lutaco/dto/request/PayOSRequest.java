package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "PayOSRequest",
        description = "Request gửi sang PayOS để khởi tạo giao dịch thanh toán. " +
                "Request này được build nội bộ ở backend, không nhận trực tiếp từ client nên không cần validate."
)
/*
 * Không cần validate request vì được build bằng builder ở phía backend
 */
public class PayOSRequest {

    @Schema(
            description = "Mã đơn hàng duy nhất trong hệ thống, dùng để đối soát giao dịch",
            example = "123456789",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer orderCode;

    @Schema(
            description = "Số tiền cần thanh toán (đơn vị: VND)",
            example = "50000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer amount;

    @Schema(
            description = "Mô tả nội dung thanh toán, sẽ hiển thị trên giao diện PayOS",
            example = "Thanh toan nang cap tai khoan Premium"
    )
    String description;

    @Schema(
            description = "URL PayOS redirect về khi người dùng huỷ thanh toán",
            example = "https://example.com/payment/cancel"
    )
    String cancelUrl;

    @Schema(
            description = "URL PayOS redirect về khi thanh toán thành công",
            example = "https://example.com/payment/success"
    )
    String returnUrl;

    @Schema(
            description = "Thời điểm hết hạn của link thanh toán (epoch time - giây)",
            example = "1710000000"
    )
    Integer expiredAt;

    @Schema(
            description = "Chữ ký (signature) dùng để xác thực request với PayOS",
            example = "a1b2c3d4e5f6"
    )
    String signature;
}



/**
 * body đầy đủ:
 * {
 * *"orderCode": 3,
 * *"amount": 5000,
 * *"description": "hello123",
 * "buyerName": "string",
 * "buyerCompanyName": "string",
 * "buyerAddress": "string",
 * "buyerEmail": "user@example.com",
 * "buyerPhone": "string",
 * "items": [
 * {
 * "name": "string",
 * "quantity": 1,
 * "price": 10,
 * "unit": "string",
 * "taxPercentage": -2
 * }
 * ],
 * *"cancelUrl": "https://www.facebook.com/",
 * *"returnUrl": "https://www.facebook.com/luanlnv",
 * "invoice": { mặc định upgrade ko cân hoá đơnnn, ghi log thủ công cũng được
 * "buyerNotGetInvoice": false,
 * "taxPercentage": -2
 * },
 * "expiredAt": 1869200504, (ko bắt buộc nhưng cứ thêm vào + 30 phút thanh toán)
 * *"signature": "486f78b55147e9464e42539a2785126ab01d8f76167f9b4e765f02e10908ea1c"
 * }
 */