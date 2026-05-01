package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.InvalidatedToken;

import java.time.Instant;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Integer> {
    @Modifying
    @Query("DELETE FROM InvalidatedToken it WHERE it.expiryTime < :expiryTimeBefore")
    void deleteAllByExpiryTimeBefore(@Param("expiryTimeBefore") Instant expiryTimeBefore);

    @Query("SELECT CASE WHEN COUNT(it) > 0 THEN true ELSE false END FROM InvalidatedToken it WHERE it.jti = :jti")
    boolean existsByRefToken(@Param("jti") String jti);
}
