package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.RefreshTokenRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.RefreshTokenService;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    RefreshTokenRepository repository;
    UserRepository userRepository;

    @NonFinal
    @Value("${jwt.refresh.expiration-time}")
    Long expirationTime;

    @Override
    public RefreshToken createRefreshToken(Long id) {
        User user = userRepository.getReferenceById(id);
        String refToken = UUID.randomUUID().toString();
        Instant now = Instant.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refToken(refToken)
                .expiryTime(now.plusMillis(expirationTime))
                .build();
        return repository.save(refreshToken);
    }

    @Override
    public RefreshToken findByTokenWithUser(String username) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void deleteRefreshToken(String username) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void deleteAllByUsername(String username) {
        repository.deleteAllByUsername(username);
    }

    @Override
    public RefreshToken findByToken(String token) {
        return repository.findByRefTokenCustom(token)
                .stream().findFirst().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    @Override
    public String getUsernameByToken(String token) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public int updateStatusUsedByRefToken(String refToken) {
        return repository.updateStatusUsed(refToken);
    }
}
