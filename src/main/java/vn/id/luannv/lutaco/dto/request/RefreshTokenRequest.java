package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "RefreshTokenRequest",
        description = "Request dùng để gửi lên token refresh(jti)"
)
public class RefreshTokenRequest {
    @Schema(
            description = "token code",
            example = "{{refreshToken}}"
    )
    String refreshToken;
}
