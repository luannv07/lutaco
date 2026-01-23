package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Otp;
import vn.id.luannv.lutaco.enumerate.OtpType;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Integer> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE otps
            SET
                verified_at = CASE
                    WHEN code = :code
                         AND verified_at IS NULL
                         AND expiry_time >= NOW()
                         AND max_attempt > 0
                    THEN NOW()
                    ELSE verified_at
                END,
                max_attempt = CASE
                    WHEN verified_at IS NULL
                         AND expiry_time >= NOW()
                         AND max_attempt > 0
                    THEN max_attempt - 1
                    ELSE max_attempt
                END
            WHERE user_id = :userId
              AND otp_type = :otpType
            """, nativeQuery = true)
    void verifyOtpAtomic(@Param("code") String code,
                         @Param("userId") String userId,
                         @Param("otpType") String otpType);

    @Query("""
            SELECT o FROM Otp o
            WHERE o.user.id = :userId
              AND o.otpType = :otpType
            """)
    Optional<Otp> findSnapshot(@Param("userId") String userId,
                               @Param("otpType") OtpType otpType);


    @Modifying
    @Query(value = """
            INSERT IGNORE INTO otps (
                code, otp_type, expiry_time,
                user_id, max_resend_count, max_attempt
            )
            VALUES (
                :code, :otpType, :expiryTime,
                :userId, :maxResend, :maxAttempt
            )
            """, nativeQuery = true)
    int insertIfNotExists(@Param("code") String code,
                           @Param("otpType") String otpType,
                           @Param("expiryTime") LocalDateTime expiryTime,
                           @Param("userId") String userId,
                           @Param("maxResend") int maxResend,
                           @Param("maxAttempt") int maxAttempt);

    @Modifying
    @Query(value = """
            UPDATE otps
            SET
                code = :code,
                expiry_time = :expiryTime,
                max_resend_count = max_resend_count - 1
            WHERE user_id = :userId
              AND otp_type = :otpType
              AND max_resend_count > 0
            """, nativeQuery = true)
    int resendOtp(@Param("code") String code,
                  @Param("expiryTime") LocalDateTime expiryTime,
                  @Param("userId") String userId,
                  @Param("otpType") String otpType);

    @Modifying
    @Query(value = """
                UPDATE otps
                SET 
                    max_resend_count = :maxResendCount
                WHERE user_id = :userId
                  AND otp_type = :otpType
                  AND max_resend_count <= 0
            """, nativeQuery = true)
    void resetMaxResendCount(@Param("userId") String userId,
                             @Param("otpType") String otpType,
                             @Param("maxResendCount") int maxResendCount);
}
