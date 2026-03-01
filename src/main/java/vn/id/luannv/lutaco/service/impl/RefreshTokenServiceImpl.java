package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.RefreshTokenRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.RefreshTokenService;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    RefreshTokenRepository refreshTokenRepository;
    UserRepository userRepository;

    @NonFinal
    @Value("${jwt.refresh.expiration-time}")
    Long expirationTime;

    @Override
    @CacheEvict(value = "refreshTokens", key = "#username")
    public RefreshToken createRefreshToken(String username) {
        log.info("[{}]: Creating refresh token for user: {}", username, username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[{}]: User {} not found when creating refresh token.", username, username);
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryTime(new Date(System.currentTimeMillis() + expirationTime))
                .user(user)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("[{}]: Refresh token created for user {}. Token ID: {}", username, username, savedToken.getToken());
        return savedToken;
    }

    @Override
    @Cacheable(value = "refreshTokens", key = "#username")
    public RefreshToken findByTokenWithUser(String username) {
        log.debug("[{}]: Attempting to find refresh token for user: {}", username, username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[{}]: User {} not found when searching for refresh token.", username, username);
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        RefreshToken token = refreshTokenRepository.findByUser(user).orElse(null);
        if (token != null) {
            log.debug("[{}]: Refresh token found for user {}.", username, username);
        } else {
            log.debug("[{}]: No refresh token found for user {}.", username, username);
        }
        return token;
    }

    @Override
    @Transactional
    @CacheEvict(value = "refreshTokens", key = "#username")
    public void deleteRefreshToken(String username) {
        log.info("[{}]: Deleting refresh token for user: {}", username, username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[{}]: User {} not found when deleting refresh token.", username, username);
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        refreshTokenRepository.deleteByUser(user);
        log.info("[{}]: Refresh token successfully deleted for user {}.", username, username);
    }

    @Override
    @Cacheable(value = "refreshTokens", key = "#token")
    public RefreshToken findByToken(String token) {
        log.debug("[unknown]: Attempting to find refresh token by token string.");
        RefreshToken foundToken = refreshTokenRepository.findByToken(token)
                .filter(refreshToken -> refreshToken.getExpiryTime().after(new Date()))
                .orElseThrow(() -> {
                    log.warn("[unknown]: Invalid or expired refresh token provided.");
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });
        log.debug("[unknown]: Refresh token found and is valid.");
        return foundToken;
    }

    @Override
    @Cacheable(value = "refreshTokens", key = "#token + 'username'")
    public String getUsernameByToken(String token) {
        log.debug("[unknown]: Attempting to get username from refresh token.");
        String username = refreshTokenRepository.findUsernameByToken(token)
                .orElseThrow(() -> {
                    log.warn("[unknown]: Username not found for provided refresh token.");
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });
        log.debug("[unknown]: Username '{}' extracted from refresh token.", username);
        return username;
    }
}
