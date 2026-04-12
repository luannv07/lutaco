package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum UserStatus {
    PENDING_VERIFICATION("config.enum.user.status.pending_verification"),
    ACTIVE("config.enum.user.status.active"),
    DISABLED_BY_USER("config.enum.user.status.disabled_by_user"),
    BANNED("config.enum.user.status.banned");
    String display;
}
