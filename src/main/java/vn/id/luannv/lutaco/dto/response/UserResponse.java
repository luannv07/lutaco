package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.dto.EnumDisplay;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {
    String id;
    String username;

    String fullName;

    String address;

    String email;

    EnumDisplay<UserGender> gender;

    EnumDisplay<UserStatus> userStatus;

    String roleName;

    String createdBy;

    LocalDateTime createdDate;

    String updatedBy;

    LocalDateTime updatedDate;

    EnumDisplay<UserPlan> userPlan;
}
