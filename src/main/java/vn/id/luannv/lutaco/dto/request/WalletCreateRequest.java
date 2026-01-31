package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import vn.id.luannv.lutaco.enumerate.WalletStatus;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Tạo wallet mới")
public class WalletCreateRequest {

    @NotBlank(message = "{input.required}")
    @Length(max = 100, message = "{input.tooLong}")
    @Schema(example = "Chi tiêu cá nhân")
    String walletName;

    @NotNull(message = "{input.required}")
    @Schema(example = "10000000")
    Long initialBalance;

    @Length(max = 255, message = "{input.tooLong}")
    String description;

    @Schema(example = "ACTIVE")
    WalletStatus status;
}
