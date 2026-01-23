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
        invalidatedTokenRepository.deleteByExpiryTimeBefore(new Date());
    }

    @Override
    public boolean existByJti(String jti) {
        log.info("existByJti {} is {}", jti, invalidatedTokenRepository.existsByJti(jti));
        return invalidatedTokenRepository.existsByJti(jti);
    }

    @Override
    public void addInvalidatedToken(String jti, Date expiryTime) {
        log.debug("addInvalidatedToken: {} {}", jti, expiryTime);

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .jti(jti)
                .expiryTime(expiryTime).build());
    }
}
