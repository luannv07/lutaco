package vn.id.luannv.lutaco.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder
@Data
public class CustomUserDetails implements UserDetails {
    String username;
    String role;
    String id;
    UserStatus status;
    UserPlan userPlan;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        log.info("isEnabled called: {} {} {}", status, UserStatus.ACTIVE, UserStatus.ACTIVE == status);
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        log.info("isCredentialsNonExpired called");
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        log.info("isAccountNonLocked called: {} {} {}", status, UserStatus.BANNED, UserStatus.BANNED == status);
        return status != UserStatus.BANNED;
    }

    @Override
    public boolean isAccountNonExpired() {
        log.info("isAccountNonExpired called");
        return true;
    }
}
