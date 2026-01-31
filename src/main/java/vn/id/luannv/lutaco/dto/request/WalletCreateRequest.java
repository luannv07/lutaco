package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.WalletStatus;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(
        name = "WalletCreateRequest",
        description = "Request dùng để tạo mới ví (wallet) cho người dùng"
)
public class WalletCreateRequest {

    @NotBlank(message = "{input.required}")
    @Size(max = 100, message = "{input.tooLong}")
    @Schema(
            description = "Tên ví",
            example = "Chi tiêu cá nhân",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String walletName;

    @NotNull(message = "{input.required}")
    @Schema(
            description = "Số dư ban đầu của ví",
            example = "10000000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long initialBalance;

    @Size(max = 255, message = "{input.tooLong}")
    @Schema(
            description = "Mô tả thêm cho ví",
            example = "Ví dùng cho chi tiêu sinh hoạt hàng tháng",
            maxLength = 255
    )
    String description;

    @Schema(
            description = "Trạng thái của ví",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE"}
    )
    WalletStatus status;
}
