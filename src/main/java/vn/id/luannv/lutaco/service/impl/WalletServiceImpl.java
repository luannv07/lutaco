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
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.policy.PlanPolicy;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.WalletService;
import vn.id.luannv.lutaco.util.LocalizationUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WalletServiceImpl implements WalletService {

    WalletRepository walletRepository;
    UserRepository userRepository;
    LocalizationUtils localizationUtils;
    PlanPolicy planPolicy;

    @Override
    public WalletResponse create(WalletCreateRequest request) {
        User user = userRepository.findByIdForUpdate(SecurityUtils.getCurrentId());
        int currentWallet = walletRepository.countWalletByUser(user);
        if (!planPolicy.canCreateWallet(user, currentWallet)) {
            log.warn("User {} has reached the maximum wallet limit for their plan.", user.getUsername());
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED);
        }
        Wallet wallet = new Wallet();
        wallet.setName(request.getWalletName());
        wallet.setUser(user);
        wallet.setBalance(request.getBalance());
        wallet.setInitialBalance(request.getBalance());
        wallet.setDescription(request.getDescription());
        wallet.setActiveFlg(true);
        walletRepository.save(wallet);
        log.info("Wallet created for user: {}", user.getUsername());

        return convertToResponse(wallet);
    }

    // this method auto use by event like: register account, can not trigger by api
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void createDefaultWallet(Long userId) {
        User user = userRepository.findByIdForUpdate(userId);
        int currentWallet = walletRepository.countWalletByUser(user);
        if (!planPolicy.canCreateWallet(user, currentWallet)) {
            log.warn("User {} has reached the maximum wallet limit for their plan.", user.getUsername());
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED);
        }
        Wallet wallet = new Wallet();
        wallet.setName(localizationUtils.getLocalizedMessage("default.wallet.name"));
        wallet.setUser(user);
        wallet.setDescription(localizationUtils.getLocalizedMessage("default.wallet.description"));
        wallet.setActiveFlg(true);
        wallet.setBalance(0L);
        wallet.setInitialBalance(0L);
        walletRepository.save(wallet);
        log.info("Default wallet created for user: {}", user.getUsername());
    }

    @Override
    public WalletResponse update(Long id, WalletCreateRequest request) {
        // This method is required by BaseService, but the main update logic uses WalletUpdateRequest.
        // You can delegate to the other update method or throw an exception if this flow is not intended.
        throw new UnsupportedOperationException("Use update(String, WalletUpdateRequest) instead.");
    }

    @Override
    public WalletResponse update(Long id, WalletUpdateRequest request) {
        Wallet wallet = getMyWalletByIdOrThrow(id);

        wallet.setName(request.getWalletName());
        wallet.setDescription(request.getDescription());
        walletRepository.save(wallet);
        return convertToResponse(wallet);
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void archiveByAdmin(String userId, String walletName) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public WalletResponse getDetail(Long id) {
        return convertToResponse(getMyWalletByIdOrThrow(id));
    }

    @Override
    public Page<WalletResponse> search(WalletFilterRequest request) {
        // This service does not support pagination search yet.
        return Page.empty();
    }

    @Override
    public List<WalletResponse> getMyWallets() {
        return walletRepository.findByUserId(SecurityUtils.getCurrentId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private Wallet getMyWalletByIdOrThrow(Long id) {
        return walletRepository.findByIdAndUserId(id, SecurityUtils.getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));
    }

    private WalletResponse convertToResponse(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        return WalletResponse.builder()
                .id(wallet.getId())
                .walletName(wallet.getName())
                .initialBalance(wallet.getInitialBalance())
                .currentBalance(wallet.getBalance())
                .description(wallet.getDescription())
                .userId(wallet.getUser() != null ? wallet.getUser().getId().toString() : null)
                .build();
    }

    @Transactional
    @Override
    public void toggle(Long id) {
        Wallet wallet = getMyWalletByIdOrThrow(id);
        wallet.setActiveFlg(!wallet.isActiveFlg());
        walletRepository.save(wallet);
    }
}
