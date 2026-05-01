package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.entity.InvalidatedToken;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.InvalidatedTokenRepository;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;

import java.time.Instant;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InvalidatedTokenServiceImpl implements InvalidatedTokenService {
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        invalidatedTokenRepository.deleteAllByExpiryTimeBefore(now);
    }

    @Override
    public boolean existByJti(String jti) {
        return invalidatedTokenRepository.existsByRefToken(jti);
    }

    @Override
    @Transactional
    public void addInvalidatedToken(String jti, Instant expiryTime) {
        try {
            InvalidatedToken token = new InvalidatedToken();
            token.setJti(jti);
            token.setExpiryTime(expiryTime);
            invalidatedTokenRepository.save(token);

        } catch (DataIntegrityViolationException e) {
            log.info("Token already invalidated: {}", jti);
        } catch (Exception e) {
            log.error("Unexpected error while saving invalidated token jti={}", jti, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
