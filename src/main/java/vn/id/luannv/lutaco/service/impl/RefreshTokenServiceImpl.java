package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.RefreshTokenRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.RefreshTokenService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Date;
import java.util.Optional;
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
    public RefreshToken createRefreshToken(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryTime(new Date(System.currentTimeMillis() + expirationTime))
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Cacheable(value = "refresh_token_by_username", key = "#username")
    public RefreshToken findByTokenWithUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        return refreshTokenRepository.findByUser(user).orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "refresh_token_by_username")
    public void deleteRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        refreshTokenRepository.deleteByUser(user);
    }
}
