package vn.id.luannv.lutaco.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.util.EndpointPolicyMatcherUtils;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static vn.id.luannv.lutaco.util.SecurityUtils.getCurrentUsername;
import static vn.id.luannv.lutaco.util.SecurityUtils.resolveClientIp;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimitingFilter extends OncePerRequestFilter {
    LocalizationUtils localizationUtils;
    Cache<String, RequestInfo> rateLimitCache;
    ObjectMapper objectMapper;
    @NonFinal
    @Value("${rate.limit.max-attemps-per-second}")
    Long rateLimitMaxAttempts;
    @NonFinal
    @Value("${rate.limit.minimum-pending-seconds-each-request}")
    Long rateLimitMiniumPendingSeconds;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return EndpointPolicyMatcherUtils.getPolicy(request.getRequestURI()) == EndpointSecurityPolicy.Policy.PUBLIC_NO_LIMIT;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = resolveClientIp(request);

        String username;
        try {
            username = getCurrentUsername();
        } catch (Exception e) {
            username = "anonymous";
        }
        Instant now = Instant.now();
        String buildKey = username + "_" + ipAddress;
        RequestInfo requestInfo = getRequestInfo(buildKey, now);

        try {
            if (requestInfo.requests.size() > rateLimitMaxAttempts)
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.SYSTEM_ERROR;
            if (e instanceof BusinessException) {
                log.info("[system]: Too many requests with key: [{}].", buildKey);
                errorCode = ErrorCode.TOO_MANY_REQUESTS;
            } else {
                log.info("[system]: System error when calling {}", RateLimitingFilter.class.getName());
            }
            response.setStatus(errorCode.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(response.getOutputStream(), BaseResponse.error(
                    errorCode,
                    localizationUtils.getLocalizedMessage(errorCode.getMessage()),
                    Map.of("retryAt", requestInfo.lastRequest.plusSeconds(Math.min(rateLimitMiniumPendingSeconds, Integer.MAX_VALUE)))
            ));
        }
    }

    private synchronized RequestInfo getRequestInfo(String key, Instant now) {
        return rateLimitCache.asMap().compute(key, (k, v) -> {

            if (v == null) {
                v = new RequestInfo();
            }

            while (!v.requests.isEmpty() &&
                    v.requests.peekFirst().plusSeconds(1).isBefore(now)) {
                v.requests.pollFirst();
            }

            if (v.requests.size() > rateLimitMaxAttempts) {
                return v;
            }

            v.requests.addLast(now);
            return v;
        });
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RequestInfo {
        Deque<Instant> requests = new ArrayDeque<>();
        @Builder.Default
        Instant lastRequest = Instant.now();
    }
}
