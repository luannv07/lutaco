package vn.id.luannv.lutaco.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "UserResponse",
        description = "Thông tin chi tiết người dùng"
)
public class UserResponse {
    String id;
    @Schema(
            description = "Tên đăng nhập của người dùng",
            example = "luannv"
    )
    String username;

    @Schema(
            description = "Họ và tên người dùng",
            example = "Nguyễn Văn Luận"
    )
    String fullName;

    @Schema(
            description = "Địa chỉ người dùng",
            example = "Hà Nội"
    )
    String address;

    @Schema(
            description = "Email người dùng",
            example = "vanluandvlp@gmail.com",
            format = "email"
    )
    String email;

    @Schema(
            description = "Giới tính người dùng"
    )
    EnumDisplay<UserGender> gender;

    @Schema(
            description = "Trạng thái người dùng"
    )
    EnumDisplay<UserStatus> userStatus;

    @Schema(
            description = "Tên role của người dùng",
            example = "USER"
    )
    String roleName;

    @Schema(
            description = "Người tạo bản ghi",
            example = "system"
    )
    String createdBy;

    @Schema(
            description = "Thời điểm tạo người dùng",
            example = "2024-01-01T10:00:00"
    )
    LocalDateTime createdDate;

    @Schema(
            description = "Người cập nhật gần nhất",
            example = "admin"
    )
    String updatedBy;

    @Schema(
            description = "Thời điểm cập nhật gần nhất",
            example = "2024-01-02T09:00:00"
    )
    LocalDateTime updatedDate;

    @Schema(
            description = "Gói người dùng hiện tại"
    )
    EnumDisplay<UserPlan> userPlan;
}
