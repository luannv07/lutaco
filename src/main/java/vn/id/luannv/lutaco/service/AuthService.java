package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;

import java.util.Date;

public interface AuthService {
    AuthenticateResponse login(LoginRequest request);

    AuthenticateResponse register(UserCreateRequest request);

    void logout(String jti, Date expiryTime);


    AuthenticateResponse refreshToken(String username, String jti, Date expiryTime);
}
