package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "Refresh_Tokens")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    Integer id;

    @Column(name = "token", unique = true, nullable = false)
    String token;

    @Column(name = "expiry_time")
    Date expiryTime;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;
}
