package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.util.LocalizationUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicController {
    LocalizationUtils localizationUtils;

    @GetMapping
    public String getUserAuditLogs(HttpServletRequest request) {
        String recorded = "Your ip address: " + SecurityUtils.resolveClientIp(request)
                + ", " + "Your agent: " + request.getHeader("User-Agent");
        log.info("[RECORDED] {}", recorded);
        return recorded;
    }
    @GetMapping("/test-locale")
    public String testLocale(@RequestHeader(value = "Accept-Language", required = false) Locale locale) {
        return localizationUtils.getLocalizedMessage("greeting.message", "John");
    }
}
