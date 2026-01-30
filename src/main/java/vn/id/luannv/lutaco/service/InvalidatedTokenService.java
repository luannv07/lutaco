package vn.id.luannv.lutaco.service;

import java.util.Date;

public interface InvalidatedTokenService {
    void deleteExpiredTokens();

    boolean existByJti(String jti);

    void addInvalidatedToken(String jti, Date expiryTime);
}
