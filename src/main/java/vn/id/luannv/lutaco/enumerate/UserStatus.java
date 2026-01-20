package vn.id.luannv.lutaco.enumerate;

import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.util.Arrays;

public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    DISABLED_BY_USER,
    BANNED
    ;
    public static boolean isValid(String userStatus) {
        return Arrays.stream(UserStatus.values()).anyMatch(u -> u.name().equals(userStatus));
    }
}
