package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.enumerate.UserType;

import java.util.Optional;

@Repository
@Transactional
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByCode(UserType code);
}
