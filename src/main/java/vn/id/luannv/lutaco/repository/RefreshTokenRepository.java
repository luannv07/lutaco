package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.RefreshToken;

import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("update RefreshToken rt set rt.activeFlg = false where rt.user.username = :username and rt.activeFlg = true")
    @Modifying
    void deleteAllByUsername(@Param("username") String username);

    @Query("select rt from RefreshToken rt where rt.refToken = :refToken and rt.activeFlg = true and rt.used = false")
    List<RefreshToken> findByRefTokenCustom(String refToken);

    void deleteByRefToken(String refToken);

    @Modifying
    @Query("update RefreshToken rt set rt.used = true where rt.refToken = :refToken and rt.activeFlg = true and rt.used = false")
    int updateStatusUsed(@Param("refToken") String refToken);
}
