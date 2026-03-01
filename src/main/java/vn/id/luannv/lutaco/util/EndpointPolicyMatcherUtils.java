package vn.id.luannv.lutaco.util;

import org.springframework.util.AntPathMatcher;
import vn.id.luannv.lutaco.config.EndpointSecurityPolicy;

import java.util.Map;

public class EndpointPolicyMatcherUtils {
    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static EndpointSecurityPolicy.Policy getPolicy(String uri) {
        return EndpointSecurityPolicy.ENDPOINT_POLICIES.entrySet().stream()
                .filter(stringPolicyEntry -> matcher.match(stringPolicyEntry.getKey(), uri))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(EndpointSecurityPolicy.Policy.AUTH_REQUIRED);
    }
}
