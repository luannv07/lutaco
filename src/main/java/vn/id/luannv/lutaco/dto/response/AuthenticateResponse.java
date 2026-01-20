package vn.id.luannv.lutaco.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(
        name = "AuthenticateResponse",
        description = "Kết quả xác thực người dùng sau khi đăng nhập hoặc đăng ký"
)
public class AuthenticateResponse {

    @Schema(
            description = "JWT token dùng để xác thực các request tiếp theo",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    String accessToken;

    @Schema(
            description = "Trạng thái xác thực thành công hay không",
            example = "true"
    )
    Boolean authenticated;

    String refreshToken;
}
