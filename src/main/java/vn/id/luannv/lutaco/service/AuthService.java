package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.RefreshTokenRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;

import java.time.Instant;

public interface AuthService {
    AuthenticateResponse login(LoginRequest request);

    AuthenticateResponse register(UserCreateRequest request);

    void logout(String jti, Instant expiryTime);


    AuthenticateResponse refreshToken(RefreshTokenRequest request, String jti, Instant expiryTime);
}
