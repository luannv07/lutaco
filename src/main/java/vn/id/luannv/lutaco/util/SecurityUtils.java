package vn.id.luannv.lutaco.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.id.luannv.lutaco.entity.CustomUserDetails;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

@Slf4j
public class SecurityUtils {
    public static String getCurrentUsername() {
        return getCurrentPrincipal().getUsername();
    }

    public static String getCurrentId() {
        return getCurrentPrincipal().getId();
    }

    public static UserPlan getCurrentUserPlan() {
        return getCurrentPrincipal().getUserPlan();
    }

    public static UserStatus getCurrentUserStatus() {
        return getCurrentPrincipal().getStatus();
    }

    private static CustomUserDetails getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            throw new BusinessException(ErrorCode.LOGIN_FAILED);

        if (authentication.getPrincipal() instanceof CustomUserDetails)
            return (CustomUserDetails) authentication.getPrincipal();

        log.info("getCurrentUserMethod: {}", authentication.getPrincipal().toString());
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
