package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import vn.id.luannv.lutaco.dto.request.UserFilterRequest;
import vn.id.luannv.lutaco.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, String> {
    @Query("""
        select u from User u
        where (:#{#request.username} is null or lower(u.username) like concat('%', lower(:#{#request.username}), '%'))
            and (:#{#request.address} is null or lower(u.address) like concat('%', lower(:#{#request.address}), '%' ))
            and (:#{#request.userStatus} is null or u.userStatus = :#{#request.userStatus})
            and (:#{#request.roleId} is null or u.role.id = :#{#request.roleId})
        order by u.createdDate desc
    """)
    Page<User> findByFilters(@Param("request") UserFilterRequest request, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User>  findByEmail(String email);
}
