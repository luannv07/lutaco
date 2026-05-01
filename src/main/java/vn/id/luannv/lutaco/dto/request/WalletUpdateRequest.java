package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletUpdateRequest {

    @Size(max = 255, message = "{validation.field.too_long}")
    String walletName;

    @Size(max = 500, message = "{validation.field.too_long}")
    String description;
}
