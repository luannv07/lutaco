package vn.id.luannv.lutaco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "invalidated_tokens",
        indexes = {
                @Index(name = "idx_invalidated_tokens_jti", columnList = "jti"),
                @Index(name = "idx_invalidated_tokens_expiry_time", columnList = "expiry_time")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedToken extends BaseEntity {

    @Column(name = "jti", nullable = false, unique = true, length = 36)
    String jti;

    @Column(name = "expiry_time", nullable = false)
    Instant expiryTime;
}