package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.UserAuditLog;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {
}
