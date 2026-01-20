package vn.id.luannv.lutaco.service;

import jakarta.servlet.http.HttpServletRequest;
import vn.id.luannv.lutaco.entity.InvalidatedToken;

import java.util.Date;

public interface InvalidatedTokenService {
    void deleteExpiredTokens();

    boolean existByJti(String jti);

    void addInvalidatedToken(String jti, Date expiryTime);
}
