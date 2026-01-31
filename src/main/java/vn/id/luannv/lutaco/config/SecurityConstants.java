package vn.id.luannv.lutaco.config;

import java.util.List;

public class SecurityConstants {
    public static final List<String> PUBLIC_URLS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/test/**",
            "/webhook/payos"
    );

    public static final List<String> PUBLIC_URLS_SHOULD_NOT_AUTH = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    );
}
