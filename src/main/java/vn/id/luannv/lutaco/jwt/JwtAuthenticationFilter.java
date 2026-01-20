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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.id.luannv.lutaco.config.SecurityConstants;
import vn.id.luannv.lutaco.entity.CustomUserDetails;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;

import java.io.IOException;
import java.net.InetAddress;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    JwtService jwtService;
    UserRepository userRepository;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        log.debug("shouldNotFilter(): {} {}", request.getRequestURI(), request.getServletPath());
        return SecurityConstants.PUBLIC_URLS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        String username = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        User entity = userRepository
                .findByUsername(username).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (jwtService.isValidToken(token))
            throw new BadCredentialsException(ErrorCode.UNAUTHORIZED.getMessage());

        CustomUserDetails customUserDetails = CustomUserDetails.builder()
                .username(username)
                .role(role)
                .status(entity.getUserStatus())
                .id(entity.getId())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
