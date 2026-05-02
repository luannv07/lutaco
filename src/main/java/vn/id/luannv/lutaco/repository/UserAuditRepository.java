package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.luannv.lutaco.entity.UserAuditLog;

import java.time.Instant;

public interface UserAuditRepository extends JpaRepository<UserAuditLog, Long>, JpaSpecificationExecutor<UserAuditLog> {
    @Modifying
    @Query("""
    DELETE FROM UserAuditLog u
    WHERE (:start IS NULL OR u.createdDate >= :start)
      AND (:end IS NULL OR u.createdDate < :end)
""")
    int deleteByCreatedDateRange(@Param("start") Instant start,
                                 @Param("end") Instant end);
}
