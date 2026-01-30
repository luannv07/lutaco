package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);

    RefreshToken findByTokenWithUser(String username);

    void deleteRefreshToken(String username);
}
