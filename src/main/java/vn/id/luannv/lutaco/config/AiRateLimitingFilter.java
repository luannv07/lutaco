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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static vn.id.luannv.lutaco.util.SecurityUtils.getCurrentUsername;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiRateLimitingFilter extends OncePerRequestFilter {

    Cache<String, AiRateLimitInfo> aiRateLimitCache;
    ObjectMapper objectMapper;
    LocalizationUtils localizationUtils;

    @NonFinal
    @Value("${app.ai.rate-limit.max-requests:3}")
    int maxRequests;

    @NonFinal
    @Value("${app.ai.rate-limit.window-minutes:5}")
    long windowMinutes;

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/v1/ai/dashboard",
            "/api/v1/ai/chat"
    );

    public AiRateLimitingFilter(
            @Qualifier("aiRateLimitCache") Cache<String, AiRateLimitInfo> aiRateLimitCache,
            ObjectMapper objectMapper,
            LocalizationUtils localizationUtils
    ) {
        this.aiRateLimitCache = aiRateLimitCache;
        this.objectMapper = objectMapper;
        this.localizationUtils = localizationUtils;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !RATE_LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String username;
        try {
            username = getCurrentUsername();
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant now = Instant.now();
        long windowSeconds = windowMinutes * 60;

        AiRateLimitInfo info = aiRateLimitCache.asMap().compute(username, (k, v) -> {
            if (v == null || now.isAfter(v.windowStart.plusSeconds(windowSeconds))) {
                return new AiRateLimitInfo(1, now);
            }
            v.count++;
            return v;
        });

        if (info.count > maxRequests) {
            Instant retryAt = info.windowStart.plusSeconds(windowSeconds);
            log.info("[ai-rate-limit] username=[{}] blocked until [{}]", username, retryAt);
            response.setStatus(ErrorCode.TOO_MANY_REQUESTS.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), BaseResponse.error(
                    ErrorCode.TOO_MANY_REQUESTS,
                    localizationUtils.getLocalizedMessage(request, ErrorCode.TOO_MANY_REQUESTS.getMessage()),
                    Map.of("retryAt", retryAt.toString())
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AiRateLimitInfo {
        int count;
        Instant windowStart;
    }
}
