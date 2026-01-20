package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.User;

import java.util.Date;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);

    RefreshToken findByTokenWithUser(String username);

    void deleteRefreshToken(String username);
}
