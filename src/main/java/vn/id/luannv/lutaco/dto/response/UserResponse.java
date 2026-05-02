package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {
    Long id;
    String username;

    String fullName;

    String email;

    UserGender gender;

    UserStatus userStatus;

    UserType roleName;

    String createdBy;

    Instant createdDate;

    String updatedBy;

    Instant updatedDate;

    UserPlan userPlan;
}
