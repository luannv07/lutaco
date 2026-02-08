package vn.id.luannv.lutaco.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.id.luannv.lutaco.dto.UserAuditFilterRequest;
import vn.id.luannv.lutaco.entity.UserAuditLog;

import java.time.LocalDate;

public interface UserAuditService {
    Page<UserAuditLog> viewUserAuditLogs(UserAuditFilterRequest filter);
    void deleteUserAuditLogs(UserAuditFilterRequest filter);
    Long manualCleanup(LocalDate startDate, LocalDate endDate);
}
