package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.entity.InvalidatedToken;
import vn.id.luannv.lutaco.repository.InvalidatedTokenRepository;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;

import java.util.Date;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InvalidatedTokenServiceImpl implements InvalidatedTokenService {
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    @Transactional
    @CacheEvict(value = "invalidatedTokens", allEntries = true)
    public void deleteExpiredTokens() {
        long deletedCount = invalidatedTokenRepository.deleteByExpiryTimeBefore(new Date());
        log.info("[system]: Cleaned up {} expired invalidated tokens.", deletedCount);
    }

    @Override
    @Cacheable(value = "invalidatedTokens", key = "#jti")
    public boolean existByJti(String jti) {
        boolean exists = invalidatedTokenRepository.existsByJti(jti);
        log.debug("[system]: Checking if JTI '{}' exists in invalidated tokens: {}.", jti, exists);
        return exists;
    }

    @Override
    @CacheEvict(value = "invalidatedTokens", key = "#jti")
    public void addInvalidatedToken(String jti, Date expiryTime) {
        log.debug("[system]: Adding invalidated token with JTI: '{}' and expiry time: '{}'.", jti, expiryTime);
        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .jti(jti)
                .expiryTime(expiryTime).build());
        log.info("[system]: Invalidated token with JTI: '{}' added successfully.", jti);
    }
}
