package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.WalletCreateRequest;
import vn.id.luannv.lutaco.dto.request.WalletFilterRequest;
import vn.id.luannv.lutaco.dto.request.WalletUpdateRequest;
import vn.id.luannv.lutaco.dto.response.WalletResponse;

import java.util.List;

public interface WalletService extends BaseService<WalletFilterRequest, WalletResponse, WalletCreateRequest, String> {

    WalletResponse update(String id, WalletUpdateRequest request);

    void archiveByAdmin(String userId, String walletName);

    List<WalletResponse> getMyWallets();

    void createDefaultWallet(String userId);
}
