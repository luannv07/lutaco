package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("config.enum.payment.status.pending"),
    PAID("config.enum.payment.status.paid"),
    CANCELLED("config.enum.payment.status.cancelled"),
    FAILED("config.enum.payment.status.failed"),
    EXPIRED("config.enum.payment.status.expired"),
    UNKNOWN("config.enum.payment.status.unknown");
    String display;
}
