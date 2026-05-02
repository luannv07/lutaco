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
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuditServiceImpl implements UserAuditService {

    private final UserAuditRepository userAuditRepository;

    @Override
    public Page<UserAuditLog> viewUserAuditLogs(UserAuditFilterRequest filter) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Fetching user audit logs with filter: {}", username, filter);
        Pageable pageable = PageRequest.of(
                filter.getPage() - 1,
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );
        Page<UserAuditLog> result = userAuditRepository.findAll(createSpecification(filter), pageable);
        log.info("[{}]: Found {} user audit logs matching the criteria.", username, result.getTotalElements());
        return result;
    }

    @Transactional
    @Override
    public void deleteUserAuditLogs(UserAuditFilterRequest filter) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to delete user audit logs with filter: {}", username, filter);
        List<UserAuditLog> logs = userAuditRepository.findAll(createSpecification(filter));

        if (logs.isEmpty()) {
            log.info("[{}]: No user audit logs found to delete with the given filter.", username);
            return;
        }

        userAuditRepository.deleteAll(logs);
        log.info("[{}]: Successfully deleted {} user audit logs matching the filter.", username, logs.size());
    }

    @Override
    @Transactional
    public Long manualCleanup(Instant startDate, Instant endDate) {

        String username = SecurityUtils.getCurrentUsername();

        // normalize time
        Instant end = endDate != null
                ? endDate.plus(1, ChronoUnit.DAYS) // inclusive
                : null;

        log.info("[{}]: Starting cleanup from {} to {}", username, startDate, end);

        long count;

        if (startDate == null && end == null) {
            count = userAuditRepository.count();

            userAuditRepository.deleteAllInBatch();

            log.info("[{}]: Cleaned ALL {} logs", username, count);
            return count;
        }

        count = userAuditRepository.deleteByCreatedDateRange(startDate, end);

        if (count == 0) {
            log.info("[{}]: No logs found between {} and {}", username, startDate, end);
            return 0L;
        }

        log.info("[{}]: Deleted {} logs between {} and {}", username, count, startDate, end);
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