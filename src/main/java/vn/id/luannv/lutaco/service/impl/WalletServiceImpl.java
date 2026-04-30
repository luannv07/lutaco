package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.WalletCreateRequest;
import vn.id.luannv.lutaco.dto.request.WalletFilterRequest;
import vn.id.luannv.lutaco.dto.request.WalletUpdateRequest;
import vn.id.luannv.lutaco.dto.response.WalletResponse;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.WalletService;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WalletServiceImpl implements WalletService {

    WalletRepository walletRepository;
    UserRepository userRepository;
    LocalizationUtils localizationUtils;

    @Override
    public WalletResponse create(WalletCreateRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void createDefaultWallet(String userId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public WalletResponse update(String id, WalletCreateRequest request) {
        // This method is required by BaseService, but the main update logic uses WalletUpdateRequest.
        // You can delegate to the other update method or throw an exception if this flow is not intended.
        throw new UnsupportedOperationException("Use update(String, WalletUpdateRequest) instead.");
    }

    @Override
    public WalletResponse update(String id, WalletUpdateRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void archiveByAdmin(String userId, String walletName) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public WalletResponse getDetail(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Page<WalletResponse> search(WalletFilterRequest request, Integer page, Integer size) {
        // This service does not support pagination search yet.
        return Page.empty();
    }

    @Override
    public List<WalletResponse> getMyWallets() {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private Wallet getMyWalletByIdOrThrow(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private WalletResponse convertToResponse(Wallet wallet) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Transactional
    @Override
    public void toggle(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
