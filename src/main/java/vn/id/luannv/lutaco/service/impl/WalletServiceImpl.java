package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.WalletCreateRequest;
import vn.id.luannv.lutaco.dto.request.WalletUpdateRequest;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.WalletStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.WalletMapper;
import vn.id.luannv.lutaco.repository.WalletRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.WalletService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WalletServiceImpl implements WalletService {

    WalletRepository walletRepository;
    WalletMapper walletMapper;
    UserRepository userRepository;

    @Override
    public Wallet create(WalletCreateRequest request) {

        String userId = SecurityUtils.getCurrentId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        long count = walletRepository.countByUser_Id(userId);
        if (count >= user.getUserPlan().getMaxWallets()) {
            throw new BusinessException(ErrorCode.WALLET_LIMIT_EXCEEDED);
        }

        Wallet wallet = walletMapper.toEntity(request);
        wallet.setUser(user);
        wallet.setStatus(
                request.getStatus() != null ? request.getStatus() : WalletStatus.ACTIVE
        );

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet update(String walletName, WalletUpdateRequest request) {

        Wallet wallet = getMywalletOrThrow(walletName);
        walletMapper.update(wallet, request);
        return walletRepository.save(wallet);
    }

    /**
     * User xoá → INACTIVE (có thể khôi phục)
     */
    @Override
    public void deleteByUser(String walletName) {

        Wallet wallet = getMywalletOrThrow(walletName);
        wallet.setStatus(WalletStatus.INACTIVE);
        walletRepository.save(wallet);
    }

    /**
     * Admin xoá → ARCHIVED (vĩnh viễn)
     */
    @Override
    public void archiveByAdmin(String userId, String walletName) {

        Wallet wallet = walletRepository
                .findByUser_IdAndWalletName(userId, walletName)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENUM_NOT_FOUND));

        wallet.setStatus(WalletStatus.ARCHIVED);
        walletRepository.save(wallet);
    }

    @Override
    public Wallet getDetail(String walletName) {
        return getMywalletOrThrow(walletName);
    }

    @Override
    public List<Wallet> getMyWallets() {
        return walletRepository.findByUser_Id(
                SecurityUtils.getCurrentId()
        );
    }

    private Wallet getMywalletOrThrow(String walletName) {
        return walletRepository
                .findByUser_IdAndWalletName(
                        SecurityUtils.getCurrentId(), walletName
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    }
}

