package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSResponse<T> {

    String code;
    String desc;
    T data;
    String signature;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PayOSDataCreated {
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

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PayOSDataDetail {
        // id có thể là orderCode hoặc là payLinkOS
        String id;
        int orderCode;
        int amount;
        int amountPaid;
        int amountRemaining;
        String status;
        Instant createdAt;
        List<Object> transactions;
    }
}

