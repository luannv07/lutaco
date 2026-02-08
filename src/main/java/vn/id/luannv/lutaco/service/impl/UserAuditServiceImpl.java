package vn.id.luannv.lutaco.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.id.luannv.lutaco.dto.UserAuditFilterRequest;
import vn.id.luannv.lutaco.entity.UserAuditLog;
import vn.id.luannv.lutaco.repository.UserAuditRepository;
import vn.id.luannv.lutaco.service.UserAuditService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuditServiceImpl implements UserAuditService {

    private final UserAuditRepository userAuditRepository;

    @Override
    public Page<UserAuditLog> viewUserAuditLogs(UserAuditFilterRequest filter) {
        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );
        return userAuditRepository.findAll(createSpecification(filter), pageable);
    }

    @Transactional
    @Override
    public void deleteUserAuditLogs(UserAuditFilterRequest filter) {
        List<UserAuditLog> logs = userAuditRepository.findAll(createSpecification(filter));
        log.info("Deleting user audit logs by filter, size={}", logs.size());

        if (logs.isEmpty()) {
            return;
        }

        userAuditRepository.deleteAll(logs);
    }

    @Transactional
    @Override
    public Long manualCleanup(LocalDate startDate, LocalDate endDate) {
        long count = 0;
        if (startDate == null && endDate == null) {
            count = userAuditRepository.count();
            userAuditRepository.deleteAll();
            return count;
        }

        LocalDateTime start = startDate == null ? LocalDateTime.now() : startDate.atStartOfDay();
        LocalDateTime end = startDate == null ? LocalDateTime.now() : endDate.plusDays(1).atStartOfDay(); // inclusive end date

        List<UserAuditLog> logs = userAuditRepository.findAll(
                (root, query, cb) -> cb.between(root.get("createdDate"), start, end)
        );

        log.info(
                "Manual cleanup user audit logs from {} to {}, size={}",
                startDate, endDate, logs.size()
        );
        count = logs.size();

        if (logs.isEmpty()) {
            return count;
        }

        userAuditRepository.deleteAll(logs);
        return count;
    }

    private Specification<UserAuditLog> createSpecification(UserAuditFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getUsername())) {
                predicates.add(cb.like(
                        cb.lower(root.get("username")),
                        "%" + filter.getUsername().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(filter.getUserAgent())) {
                predicates.add(cb.like(
                        cb.lower(root.get("userAgent")),
                        "%" + filter.getUserAgent().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(filter.getClientIp())) {
                predicates.add(cb.equal(root.get("clientIp"), filter.getClientIp()));
            }

            if (StringUtils.hasText(filter.getRequestUri())) {
                predicates.add(cb.like(
                        cb.lower(root.get("requestUri")),
                        "%" + filter.getRequestUri().toLowerCase() + "%"
                ));
            }

            if (filter.getExecutionTimeMsFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("executionTimeMs"),
                        filter.getExecutionTimeMsFrom()
                ));
            }

            if (filter.getExecutionTimeMsTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("executionTimeMs"),
                        filter.getExecutionTimeMsTo()
                ));
            }

            if (StringUtils.hasText(filter.getParamContent())) {
                predicates.add(cb.like(
                        cb.lower(root.get("paramContent")),
                        "%" + filter.getParamContent().toLowerCase() + "%"
                ));
            }

            if (filter.getCreatedDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdDate"),
                        filter.getCreatedDateFrom().atStartOfDay()
                ));
            }

            if (filter.getCreatedDateTo() != null) {
                LocalDateTime endDateTime = filter.getCreatedDateTo()
                        .plusDays(1)
                        .atStartOfDay();

                predicates.add(cb.lessThan(
                        root.get("createdDate"),
                        endDateTime
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}