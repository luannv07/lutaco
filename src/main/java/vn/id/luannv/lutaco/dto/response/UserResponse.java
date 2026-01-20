package vn.id.luannv.lutaco.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import vn.id.luannv.lutaco.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

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
}
