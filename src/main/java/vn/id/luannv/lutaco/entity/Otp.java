package vn.id.luannv.lutaco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vn.id.luannv.lutaco.enumerate.OtpType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "OTPS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_otp_user_id_otp_type",
                        columnNames = {"USER_ID", "OTP_TYPE"}
                )
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    Integer id;

    @Column(name = "CODE", nullable = false, length = 6)
    String code;

    @Column(name = "OTP_TYPE", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    OtpType otpType;

    @Column(name = "EXPIRY_TIME", nullable = false)
    LocalDateTime expiryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "USER_ID",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_otps_user")
    )
    @JsonIgnore
    User user;

    @Column(name = "max_resend_count", nullable = false)
    @JsonIgnore
    Integer maxResendCount;

    @Column(name = "max_attempt", nullable = false)
    @JsonIgnore
    Integer maxAttempt;

    @Column(name = "verified_at")
    @JsonIgnore
    LocalDateTime verifiedAt;
}
