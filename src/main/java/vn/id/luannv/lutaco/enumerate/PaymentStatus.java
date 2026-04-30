package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

public enum PaymentStatus {
    PENDING,
    PAID,
    CANCELLED,
    FAILED,
    EXPIRED,
    UNKNOWN;
}
