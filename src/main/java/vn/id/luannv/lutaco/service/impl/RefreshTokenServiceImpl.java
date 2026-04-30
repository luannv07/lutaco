package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.service.RefreshTokenService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Override
    public RefreshToken createRefreshToken(String username) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
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
    public RefreshToken findByToken(String token) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public String getUsernameByToken(String token) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
