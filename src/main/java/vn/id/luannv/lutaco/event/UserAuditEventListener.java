package vn.id.luannv.lutaco.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.entity.UserAuditLog;
import vn.id.luannv.lutaco.event.entity.UserAuditEvent;
import vn.id.luannv.lutaco.repository.UserAuditLogRepository;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAuditEventListener {

    UserAuditLogRepository repository;

    @Async
    @EventListener
    public void handle(UserAuditEvent event) {

        UserAuditLog auditLog = UserAuditLog.builder()
                .username(event.getUsername())
                .userAgent(event.getUserAgent())
                .clientIp(event.getClientIp())
                .requestUri(event.getRequestUri())
                .methodName(event.getMethodName())
                .executionTimeMs(event.getExecutionTimeMs())
                .paramContent(event.getParamContent())
                .build();

        repository.save(auditLog);

        log.info("[{}]: User audit log saved for method: {}, execution time: {}ms",
                event.getUsername(),
                event.getMethodName(),
                event.getExecutionTimeMs()
        );
    }
}
