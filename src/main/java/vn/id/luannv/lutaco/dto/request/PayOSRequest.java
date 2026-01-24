package vn.id.luannv.lutaco.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * ko cần validate request bởi vì dùng builder ở phía be
 */
public class PayOSRequest {
    Integer orderCode;
    Integer amount;
    String description;
    String cancelUrl;
    String returnUrl;
    Integer expiredAt;
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