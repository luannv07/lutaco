package vn.id.luannv.lutaco.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "AuthenticateResponse",
        description = "Response trả về sau khi người dùng đăng nhập hoặc đăng ký thành công"
)
public class AuthenticateResponse {

    @Schema(
            description = "JWT Access Token dùng để xác thực cho các request tiếp theo",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    String accessToken;

    @Schema(
            description = "JWT Refresh Token dùng để cấp lại access token khi hết hạn",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh..."
    )
    String refreshToken;

    @Schema(
            description = "Trạng thái xác thực người dùng",
            example = "true"
    )
    Boolean authenticated;
}
