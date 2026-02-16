package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);

    RefreshToken findByTokenWithUser(String username);

    // token thực chất là key refreshToken khi login thành công
    RefreshToken findByToken(String token);

    String getUsernameByToken(String token);

    void deleteRefreshToken(String username);
}
