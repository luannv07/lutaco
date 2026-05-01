package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
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

    @Data
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PayOSDataDetail {

        String id;

        int orderCode;

        int amount;

        int amountPaid;

        int amountRemaining;

        String status;

        Instant createdAt;

        List<Object> transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PayOSDataByUser {

        Integer orderCode;

        String paymentLinkId;

        Integer amount;

        String currency;

        String description;

        String status;

        String type;

        LocalDateTime createdDate;

        LocalDateTime paidAt;
    }
}
