package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;

@Getter
@Setter
@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    String username;

    @Column(nullable = false)
    String password;

    @Column(name = "full_name", nullable = false, length = 255)
    String fullName;

    @Column(nullable = false, unique = true, length = 255)
    String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserGender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_plan", nullable = false, length = 20)
    UserPlan userPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;
}