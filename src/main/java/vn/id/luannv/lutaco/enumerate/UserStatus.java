package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    DISABLED_BY_USER,
    BANNED;
}
