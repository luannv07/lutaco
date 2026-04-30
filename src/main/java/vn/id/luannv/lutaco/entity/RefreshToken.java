package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Column(name = "ref_token", nullable = false, unique = true, length = 36)
    String refToken;

    @Column(name = "expiry_time", nullable = false)
    Instant expiryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    // tạm thời chưa cần làm
    @Column(name = "device_info", length = 255)
    String deviceInfo;

    @Builder.Default
    @Column(name = "used")
    Boolean used = Boolean.FALSE;

    @Builder.Default
    @Column(name = "active_flg")
    Boolean activeFlg = Boolean.TRUE;
}