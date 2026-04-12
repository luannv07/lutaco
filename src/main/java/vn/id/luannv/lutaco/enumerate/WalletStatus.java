package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum WalletStatus {
    ACTIVE("config.enum.wallet.status.active"),
    INACTIVE("config.enum.wallet.status.inactive"),
    ARCHIVED("config.enum.wallet.status.archived");
    String display;
}
