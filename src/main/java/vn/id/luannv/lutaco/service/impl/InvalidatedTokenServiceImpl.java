package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    public void deleteExpiredTokens() {
        long deletedCount = invalidatedTokenRepository.deleteByExpiryTimeBefore(new Date());
        log.info("Cleaned up {} expired invalidated tokens.", deletedCount);
    }

    @Override
    public boolean existByJti(String jti) {
        boolean exists = invalidatedTokenRepository.existsByJti(jti);
        log.debug("Checking if JTI '{}' exists in invalidated tokens: {}.", jti, exists);
        return exists;
    }

    @Override
    public void addInvalidatedToken(String jti, Date expiryTime) {
        log.debug("Adding invalidated token with JTI: '{}' and expiry time: '{}'.", jti, expiryTime);
        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .jti(jti)
                .expiryTime(expiryTime).build());
        log.info("Invalidated token with JTI: '{}' added successfully.", jti);
    }
}
