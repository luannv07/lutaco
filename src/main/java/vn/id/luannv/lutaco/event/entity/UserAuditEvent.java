package vn.id.luannv.lutaco.event.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAuditEvent {
    String username;
    String userAgent;
    String clientIp;
    String requestUri;
    String methodName;
    Long executionTimeMs;
}
