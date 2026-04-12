package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.util.LocalizationUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.io.InputStream;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicController {
    LocalizationUtils localizationUtils;
    CacheManager cacheManager;

    @GetMapping("/api/v1/public/audit")
    public ResponseEntity<BaseResponse<String>> getClientAuditInfo(HttpServletRequest request) {
        String clientIp = SecurityUtils.resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String recordedInfo = String.format("Client IP: %s, User-Agent: %s", clientIp, userAgent);
        log.info("[unknown]: Client audit info recorded: {}", recordedInfo);
        return ResponseEntity.ok(BaseResponse.success(recordedInfo, "Client audit information recorded."));
    }

    @GetMapping("/api/v1/public/test-locale")
    public ResponseEntity<BaseResponse<String>> testLocale(@RequestHeader(value = "Accept-Language", required = false) Locale locale) {
        String message = localizationUtils.getLocalizedMessage("greeting.message", "John");
        log.info("[unknown]: Testing locale with Accept-Language: {}. Localized message: {}", locale, message);
        return ResponseEntity.ok(BaseResponse.success(message, "Localization test successful."));
    }

    @PostMapping("/api/v1/public/clear-cache")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> clearAllCaches() {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to clear all caches.", username);
        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            log.info("[{}]: Cache '{}' cleared.", username, cacheName);
        });
        log.info("[{}]: All caches cleared successfully.", username);
        return ResponseEntity.ok(BaseResponse.success("All caches cleared successfully."));
    }

    @GetMapping("/api/v1/public/translations")
    @Cacheable("translationCache")
    public ResponseEntity<BaseResponse<Map<String, Map<String, String>>>> getConfigsOnly() {
        Map<String, Map<String, String>> allConfigs = new HashMap<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            // CHỈ quét các file bắt đầu bằng "configs_"
            Resource[] resources = resolver.getResources("classpath:i18n/configs_*.properties");

            for (Resource resource : resources) {
                Properties props = new Properties();
                try (InputStream is = resource.getInputStream()) {
                    props.load(is);

                    // Lấy phần ngôn ngữ (ví dụ: configs_vi.properties -> vi)
                    String fileName = resource.getFilename();
                    // Logic: lấy chuỗi nằm giữa dấu "_" đầu tiên và dấu "." cuối cùng
                    String langCode = fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("."));

                    Map<String, String> langMap = new HashMap<>();
                    props.forEach((k, v) -> langMap.put(k.toString(), v.toString()));

                    allConfigs.put(langCode, langMap);
                }
            }
        } catch (Exception e) {
            log.error("[system]: Can not read files configs_*.properties. Error message: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CACHE_CONTROL, "public, max-age=86400");

        return ResponseEntity.ok().headers(httpHeaders).body(BaseResponse.success(allConfigs, "Đã tải configs tĩnh."));
    }

    @GetMapping("/")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Application is running smoothly 36363636366363676767676776!");
    }

}
