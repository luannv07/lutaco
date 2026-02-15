package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.util.LocalizationUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicController {
    LocalizationUtils localizationUtils;
    CacheManager cacheManager;

    @GetMapping("/audit")
    public ResponseEntity<BaseResponse<String>> getClientAuditInfo(HttpServletRequest request) {
        String clientIp = SecurityUtils.resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String recordedInfo = String.format("Client IP: %s, User-Agent: %s", clientIp, userAgent);
        log.info("Client audit info recorded: {}", recordedInfo);
        return ResponseEntity.ok(BaseResponse.success(recordedInfo, "Client audit information recorded."));
    }

    @GetMapping("/test-locale")
    public ResponseEntity<BaseResponse<String>> testLocale(@RequestHeader(value = "Accept-Language", required = false) Locale locale) {
        String message = localizationUtils.getLocalizedMessage("greeting.message", "John");
        log.info("Testing locale with Accept-Language: {}. Localized message: {}", locale, message);
        return ResponseEntity.ok(BaseResponse.success(message, "Localization test successful."));
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<BaseResponse<Void>> clearAllCaches() {
        log.info("Attempting to clear all caches.");
        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            log.info("Cache '{}' cleared.", cacheName);
        });
        log.info("All caches cleared successfully.");
        return ResponseEntity.ok(BaseResponse.success("All caches cleared successfully."));
    }
}
