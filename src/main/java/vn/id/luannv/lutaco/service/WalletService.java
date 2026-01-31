package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.WalletCreateRequest;
import vn.id.luannv.lutaco.dto.request.WalletUpdateRequest;
import vn.id.luannv.lutaco.entity.Wallet;

import java.util.List;

public interface WalletService {

    Wallet create(WalletCreateRequest request);

    Wallet update(String walletName, WalletUpdateRequest request);

    void deleteByUser(String walletName);

    void archiveByAdmin(String userId, String walletName);

    Wallet getDetail(String walletName);

    List<Wallet> getMyWallets();

    void createDefaultWallet(String userId);
}
