package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.InvalidatedToken;

import java.util.Date;
@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Integer> {

    boolean existsByJti(String jti);

    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiryTime < :now")
    void deleteByExpiryTimeBefore(@Param("now") Date now);
}
