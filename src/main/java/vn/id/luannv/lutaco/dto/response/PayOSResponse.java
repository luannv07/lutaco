package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSResponse {

    String code;
    String desc;
    PayOSData data;
    String signature;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PayOSData {
        String bin;
        String accountNumber;
        String accountName;
        Long amount;
        String description;
        Integer orderCode;
        String currency;
        String paymentLinkId;
        String status;
        String checkoutUrl;
        String qrCode;
    }
}

