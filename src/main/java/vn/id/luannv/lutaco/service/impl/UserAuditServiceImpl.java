package vn.id.luannv.lutaco.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.UserAuditFilterRequest;
import vn.id.luannv.lutaco.entity.UserAuditLog;
import vn.id.luannv.lutaco.service.UserAuditService;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuditServiceImpl implements UserAuditService {

    @Override
    public Page<UserAuditLog> viewUserAuditLogs(UserAuditFilterRequest filter) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Transactional
    @Override
    public void deleteUserAuditLogs(UserAuditFilterRequest filter) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Transactional
    @Override
    public Long manualCleanup(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
