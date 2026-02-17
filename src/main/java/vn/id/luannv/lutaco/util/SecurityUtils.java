package vn.id.luannv.lutaco.util;

import jakarta.servlet.http.HttpServletRequest;
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
    public static String getCurrentRoleName() {
        return getCurrentPrincipal().getRole();
    }
    private static CustomUserDetails getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[system]: Authentication object is null or not authenticated.");
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        log.error("[system]: Principal is not of type CustomUserDetails. Actual type: {}", authentication.getPrincipal().getClass().getName());
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP",
                "True-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                log.debug("[system]: Resolved client IP '{}' from header '{}'.", ip, header);
                return ip.split(",")[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        log.debug("[system]: Resolved client IP '{}' from request.getRemoteAddr().", remoteAddr);
        return remoteAddr;
    }
}
