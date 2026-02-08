package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.id.luannv.lutaco.entity.UserAuditLog;

public interface UserAuditRepository extends JpaRepository<UserAuditLog, Long>, JpaSpecificationExecutor<UserAuditLog> {
}
