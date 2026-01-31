package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.util.SecurityUtils;

@Slf4j
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicController {
    @GetMapping
    public String getUserAuditLogs(HttpServletRequest request) {
        String recorded = "Your ip address: " + SecurityUtils.resolveClientIp(request)
                + ", " + "Your agent: " + request.getHeader("User-Agent");
        log.info("[RECORDED] {}", recorded);
        return recorded;
    }
}
