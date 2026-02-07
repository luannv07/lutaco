package vn.id.luannv.lutaco.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletSummaryResponse {
    String walletName;
    Long balance;
}
