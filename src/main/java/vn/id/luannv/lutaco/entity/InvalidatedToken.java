package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
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
                @Index(name = "idx_invalidated_tokens_ref_token", columnList = "ref_token"),
                @Index(name = "idx_invalidated_tokens_expiry_time", columnList = "expiry_time")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedToken extends BaseEntity {

    @Column(name = "ref_token", nullable = false, unique = true, length = 36)
    String refToken;

    @Column(name = "expiry_time", nullable = false)
    Instant expiryTime;
}