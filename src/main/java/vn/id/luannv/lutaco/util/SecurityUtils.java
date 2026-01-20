package vn.id.luannv.lutaco.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import vn.id.luannv.lutaco.entity.CustomUserDetails;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

@Slf4j
public class SecurityUtils {
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public static String getCurrentRole() {
        return getCurrentUser().getRole();
    }

    public static String getCurrentId() {
        return getCurrentUser().getId();
    }

    private static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        log.info("getCurrentUserMethod: {}", authentication.getPrincipal().toString());
        throw new BusinessException(ErrorCode.LOGIN_FAILED);
    }
}
