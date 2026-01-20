package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.luannv.lutaco.entity.InvalidatedToken;
import vn.id.luannv.lutaco.entity.MasterDictionary;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Integer> {

    boolean existsByJti(String jti);

    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiryTime < :now")
    void deleteByExpiryTimeBefore(@Param("now") Date now);
}
