package vn.id.luannv.lutaco.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.entity.CustomUserDetails;
import vn.id.luannv.lutaco.enumerate.UserStatus;

@Component("securityPermission")
public class SecurityPermission {
    public boolean isActive() {
        return checkStatus(UserStatus.ACTIVE);
    }

    public boolean isPendingVerification() {
        return checkStatus(UserStatus.PENDING_VERIFICATION);
    }

    public boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private boolean checkStatus(UserStatus status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails user)) {
            return false;
        }

        return user.getStatus() == status;
    }
}
