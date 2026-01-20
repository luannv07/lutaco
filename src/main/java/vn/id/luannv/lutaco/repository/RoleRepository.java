package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.request.UserFilterRequest;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Integer id(Integer id);

    Optional<Role> findByName(String name);
}
