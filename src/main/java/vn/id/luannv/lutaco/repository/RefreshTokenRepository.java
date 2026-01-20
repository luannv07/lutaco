package vn.id.luannv.lutaco.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.service.RefreshTokenService;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user")
    void deleteByUser(@Param("user") User user);
}
