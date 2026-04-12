package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.dto.EnumDisplay;
import vn.id.luannv.lutaco.enumerate.WalletStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletResponse {
    String id;
    String walletName;
    Long initialBalance;
    Long currentBalance;
    String description;
    EnumDisplay<WalletStatus> status;
    String userId;
}
