package vn.id.luannv.lutaco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.UserStatus;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", updatable = false, nullable = false)
    String id;
    @Column(name = "USERNAME", unique = true, nullable = false)
    String username;
    @Column(name = "PASSWORD", nullable = false)
    String password;
    @Column(name = "FULL_NAME", nullable = false)
    String fullName;
    @Column(name = "ADDRESS")
    String address;
    @Column(name = "EMAIL", nullable = false)
    String email;
    @Column(name = "GENDER", nullable = false)
    String gender;
    @Column(name = "USER_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    UserStatus userStatus;
    @JoinColumn(name = "ROLE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    Role role;
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    RefreshToken refreshToken;
}
