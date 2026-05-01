package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    String walletName;

    @NotNull(message = "{validation.required}")
    @PositiveOrZero(message = "{validation.field.positive_or_zero}")
    Long balance;

    @Size(max = 500, message = "{validation.field.too_long}")
    String description;
}
