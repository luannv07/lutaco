package vn.id.luannv.lutaco.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.id.luannv.lutaco.config.EndpointSecurityPolicy;
import vn.id.luannv.lutaco.entity.CustomUserDetails;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.util.EndpointPolicyMatcherUtils;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    JwtService jwtService;
    UserRepository userRepository;
    LocalizationUtils localizationUtils;
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return EndpointPolicyMatcherUtils.getPolicy(request.getRequestURI()) != EndpointSecurityPolicy.Policy.AUTH_REQUIRED;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.debug("[system]: Authorization header not found or does not start with 'Bearer ' for request URI: {}", request.getRequestURI());
                throw new BadCredentialsException(localizationUtils.getLocalizedMessage(ErrorCode.UNAUTHORIZED.getMessage()));
            }

            String token = authorizationHeader.substring(7);
            if (!jwtService.isValidToken(token)) {
                log.warn("[system]: Invalid or expired JWT token for request URI: {}", request.getRequestURI());
                throw new BadCredentialsException(localizationUtils.getLocalizedMessage(ErrorCode.UNAUTHORIZED.getMessage()));
            }

            String username = jwtService.getUsernameFromToken(token);
            String role = jwtService.getRoleFromToken(token);
            User entity = userRepository
                    .findByUsername(username).orElseThrow(() -> {
                        log.warn("[system]: User not found for username extracted from token: {}", username);
                        return new BadCredentialsException(localizationUtils.getLocalizedMessage(ErrorCode.UNAUTHORIZED.getMessage()));
                    });

            CustomUserDetails customUserDetails = CustomUserDetails.builder()
                    .username(username)
                    .role(role)
                    .status(entity.getUserStatus())
                    .id(entity.getId())
                    .userPlan(entity.getUserPlan())
                    .build();
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ae) {
            SecurityContextHolder.clearContext();
            log.error("[system]: Authentication failed during JWT filter for request URI: {}. Error: {}", request.getRequestURI(), ae.getMessage());
            jwtAuthenticationEntryPoint.commence(request, response, ae);
        }
    }
}
