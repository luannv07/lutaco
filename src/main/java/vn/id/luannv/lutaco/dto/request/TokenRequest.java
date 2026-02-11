package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "TokenRequest",
        description = "Request chứa token xác thực (thường dùng cho verify / refresh / logout)"
)
public class TokenRequest {

    @NotBlank(message = "{input.required}")
    @Length(max = 1000, message = "{validation.field.too_long}")
    @Schema(
            description = "JWT token cần xử lý",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    String token;
}
