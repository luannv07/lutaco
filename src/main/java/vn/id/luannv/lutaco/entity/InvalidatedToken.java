package vn.id.luannv.lutaco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Invalidated_Tokens")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class InvalidatedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    Integer id;
    @Column(name = "jti", unique = true, nullable = false)
    String jti;
    @Column(name = "expiry_time")
    Date expiryTime;
}
