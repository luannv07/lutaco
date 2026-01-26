package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {
    String username;
    String fullName;
    String address;
    String email;
    String gender;
    String userStatus;
    String roleName;
    String createdBy;
    LocalDateTime createdDate;
    String updatedBy;
    LocalDateTime updatedDate;
    String userPlan;
}
