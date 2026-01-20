package vn.id.luannv.lutaco.config;

import java.util.List;

public class SecurityConstants {
    public static final List<String> PUBLIC_URLS = List.of(
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/test/**"
    );
}
