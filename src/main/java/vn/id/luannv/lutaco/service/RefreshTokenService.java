package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long id);

    RefreshToken findByTokenWithUser(String username);

    // token thực chất là key refreshToken khi login thành công
    RefreshToken findByToken(String token);

    String getUsernameByToken(String token);

    void deleteRefreshToken(String username);

    void deleteAllByUsername(String username);

    void deleteById(Long id);

    int updateStatusUsedByRefToken(String refToken);
}
