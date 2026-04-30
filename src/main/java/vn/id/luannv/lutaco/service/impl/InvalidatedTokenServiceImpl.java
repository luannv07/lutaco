package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;

import java.util.Date;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InvalidatedTokenServiceImpl implements InvalidatedTokenService {

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public boolean existByJti(String jti) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void addInvalidatedToken(String jti, Date expiryTime) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
