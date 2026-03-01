package vn.id.luannv.lutaco.config;

import java.util.Map;

public class EndpointSecurityPolicy {
    public static final Map<String, Policy> ENDPOINT_POLICIES = Map.ofEntries(
            Map.entry("/api/v1/auth/login", Policy.PUBLIC_RATE_LIMITED),
            Map.entry("/api/v1/auth/register", Policy.PUBLIC_RATE_LIMITED),
            Map.entry("/api/v1/auth/refresh-token", Policy.PUBLIC_RATE_LIMITED),

            Map.entry("/swagger-ui/**", Policy.PUBLIC_NO_LIMIT),
            Map.entry("/swagger-ui.html", Policy.PUBLIC_NO_LIMIT),
            Map.entry("/v3/api-docs/**", Policy.PUBLIC_NO_LIMIT),
            Map.entry("/webjars/**", Policy.PUBLIC_NO_LIMIT),

            Map.entry("/webhook/payos", Policy.PUBLIC_RATE_LIMITED),

            Map.entry("/api/v1/public/**", Policy.PUBLIC_RATE_LIMITED),
            Map.entry("/public/**", Policy.PUBLIC_RATE_LIMITED),

            Map.entry("/error", Policy.PUBLIC_NO_LIMIT)
    );

    public enum Policy {
        PUBLIC_RATE_LIMITED,
        PUBLIC_NO_LIMIT,
        AUTH_REQUIRED
    }
}
