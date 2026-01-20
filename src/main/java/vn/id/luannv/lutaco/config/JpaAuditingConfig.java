package vn.id.luannv.lutaco.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.id.luannv.lutaco.entity.CustomUserDetails;

import java.util.Optional;

@Slf4j
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken)
                return Optional.of("system");

            log.debug("Authentication auditorProvider: {}", authentication.getName());
            return Optional.of(((CustomUserDetails) authentication.getPrincipal()).getUsername());
        };
    }
}

