package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.WalletCreateRequest;
import vn.id.luannv.lutaco.dto.request.WalletUpdateRequest;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.WalletStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.WalletMapper;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.service.WalletService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WalletServiceImpl implements WalletService {

    WalletRepository walletRepository;
    WalletMapper walletMapper;
    UserRepository userRepository;

    @Override
    @CacheEvict(value = "wallets", key = "@securityPermission.getCurrentUserId()")
    public Wallet create(WalletCreateRequest request) {
        String userId = SecurityUtils.getCurrentId();
        log.info("Attempting to create wallet for user ID: {}. Request: {}", userId, request.getWalletName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found during wallet creation.", userId);
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        long count = walletRepository.countByUser_Id(userId);
        if (count >= user.getUserPlan().getMaxWallets()) {
            log.warn("User ID {} has reached maximum wallet limit ({}). Cannot create new wallet.",
                    userId, user.getUserPlan().getMaxWallets());
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED);
        }

        Wallet wallet = walletMapper.toEntity(request);
        wallet.setUser(user);
        wallet.setStatus(WalletStatus.ACTIVE);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet '{}' (ID: {}) created successfully for user ID {}.",
                savedWallet.getWalletName(), savedWallet.getId(), userId);
        return savedWallet;
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    @CacheEvict(value = "wallets", key = "@securityPermission.getCurrentUserId()")
    public void createDefaultWallet(String userId) {
        log.info("Attempting to create default wallet for user ID: {}.", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found during default wallet creation.", userId);
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        long count = walletRepository.countByUser_Id(userId);
        if (count >= user.getUserPlan().getMaxWallets()) {
            log.warn("User ID {} has reached maximum wallet limit ({}). Cannot create default wallet.",
                    userId, user.getUserPlan().getMaxWallets());
            throw new BusinessException(ErrorCode.OPERATION_LIMIT_EXCEEDED);
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName("Ví mặc định");
        wallet.setInitialBalance(0L);
        wallet.setCurrentBalance(0L);
        wallet.setDescription("Ví mặc định do hệ thống tạo.");
        wallet.setStatus(WalletStatus.ACTIVE);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Default wallet '{}' (ID: {}) created successfully for user ID {}.",
                savedWallet.getWalletName(), savedWallet.getId(), userId);
    }

    @Override
    @CacheEvict(value = "wallets", key = "@securityPermission.getCurrentUserId()")
    public Wallet update(String walletName, WalletUpdateRequest request) {
        log.info("Attempting to update wallet '{}' for current user. Request: {}", walletName, request);
        Wallet wallet = getMywalletOrThrow(walletName);
        walletMapper.update(wallet, request);
        Wallet updatedWallet = walletRepository.save(wallet);
        log.info("Wallet '{}' (ID: {}) updated successfully.",
                updatedWallet.getWalletName(), updatedWallet.getId());
        return updatedWallet;
    }

    @Override
    @CacheEvict(value = "wallets",
            key = "#walletName + '_' + " + "@securityPermission.getCurrentUserId()")
    public void deleteByUser(String walletName) {
        log.info("Attempting to soft delete wallet '{}' for current user.", walletName);
        Wallet wallet = getMywalletOrThrow(walletName);
        wallet.setStatus(WalletStatus.INACTIVE);
        walletRepository.save(wallet);
        log.info("Wallet '{}' (ID: {}) soft deleted successfully.",
                wallet.getWalletName(), wallet.getId());
    }

    @Override
    @CacheEvict(value = "wallets",
            key = "#walletName + '_' + #userId")
    public void archiveByAdmin(String userId, String walletName) {
        log.info("Admin attempting to archive wallet '{}' for user ID: {}.", walletName, userId);

        Wallet wallet = walletRepository
                .findByUser_IdAndWalletName(userId, walletName)
                .orElseThrow(() -> {
                    log.warn("Wallet '{}' not found for user ID {} for archiving.", walletName, userId);
                    return new BusinessException(ErrorCode.ENUM_NOT_FOUND);
                });

        wallet.setStatus(WalletStatus.ARCHIVED);
        walletRepository.save(wallet);
        log.info("Wallet '{}' (ID: {}) archived successfully for user ID {}.",
                wallet.getWalletName(), wallet.getId(), userId);
    }

    @Override
    @Cacheable(value = "wallets",
            key = "#walletName + '_' + " + "@securityPermission.getCurrentUserId()")
    public Wallet getDetail(String walletName) {
        log.info("Fetching details for wallet '{}' for current user.", walletName);
        Wallet wallet = getMywalletOrThrow(walletName);
        log.info("Successfully retrieved details for wallet '{}' (ID: {}).",
                wallet.getWalletName(), wallet.getId());
        return wallet;
    }

    @Override
    @Cacheable(value = "wallets", key = "@securityPermission.getCurrentUserId()")
    public List<Wallet> getMyWallets() {
        String currentUserId = SecurityUtils.getCurrentId();
        log.info("Fetching all wallets for current user ID: {}.", currentUserId);
        List<Wallet> wallets = walletRepository.findByUser_Id(currentUserId);
        log.info("Found {} wallets for user ID {}.", wallets.size(), currentUserId);
        return wallets;
    }

    private Wallet getMywalletOrThrow(String walletName) {
        String currentUserId = SecurityUtils.getCurrentId();
        return walletRepository
                .findByUser_IdAndWalletName(currentUserId, walletName)
                .orElseThrow(() -> {
                    log.warn("Wallet '{}' not found for current user ID {}.", walletName, currentUserId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });
    }
}