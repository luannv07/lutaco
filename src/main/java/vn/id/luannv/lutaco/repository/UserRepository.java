package vn.id.luannv.lutaco.repository;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.User;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u join fetch u.role where u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    boolean existsByUsernameOrEmail(String username, String email);

    @Query("select u from User u join fetch u.role where u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    User findByIdForUpdate(Long id);
}
