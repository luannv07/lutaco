package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "WalletUpdateRequest",
        description = "Request dùng để cập nhật thông tin ví (wallet)"
)
public class WalletUpdateRequest {

    @NotBlank(message = "{input.required}")
    @Size(max = 100, message = "{input.tooLong}")
    @Schema(
            description = "Tên ví",
            example = "Chi tiêu cá nhân",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String walletName;

    @Size(max = 255, message = "{input.tooLong}")
    @Schema(
            description = "Mô tả thêm cho ví",
            example = "Ví dùng cho chi tiêu sinh hoạt hàng tháng",
            maxLength = 255
    )
    String description;
}
