package vn.id.luannv.lutaco.service;

import org.springframework.data.domain.Page;
import vn.id.luannv.lutaco.dto.UserAuditFilterRequest;
import vn.id.luannv.lutaco.entity.UserAuditLog;

import java.time.Instant;
import java.time.LocalDate;

public interface UserAuditService {
    Page<UserAuditLog> viewUserAuditLogs(UserAuditFilterRequest filter);

    void deleteUserAuditLogs(UserAuditFilterRequest filter);

    Long manualCleanup(Instant startDate, Instant endDate);
}
