package vn.id.luannv.lutaco.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.util.SecurityUtils;

@Component("securityPermission")
public class SecurityPermission {
    public boolean isActive() {
        return SecurityUtils.getCurrentUserStatus() == UserStatus.ACTIVE;
    }

    public boolean isPendingVerification() {
        return SecurityUtils.getCurrentUserStatus() == UserStatus.PENDING_VERIFICATION;
    }

    public boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    public boolean isPremiumUser() {
        return SecurityUtils.getCurrentUserPlan() == UserPlan.PREMIUM;
    }

    public String getCurrentUserId() {
        return SecurityUtils.getCurrentId();
    }
}
