package vn.id.luannv.lutaco.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JwtService {
    InvalidatedTokenService invalidatedTokenService;

    @NonFinal
    @Value("${jwt.secret-key}")
    String secretKey;

    @NonFinal
    @Value("${jwt.expiration-time}")
    Long expirationTime;

    @NonFinal
    @Value("role")
    String roleClaim;

    public String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .issuer("lutaco")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expirationTime))
                .claim(roleClaim, "ROLE_" + user.getRole().getName())
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes()));
        } catch (JOSEException e) {
            log.error("[system]: Failed to generate JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }

        return jwsObject.serialize();
    }

    private JWTClaimsSet jwtClaimsSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(secretKey);
            if (!signedJWT.verify(verifier)) {
                log.warn("[system]: JWT token verification failed: Invalid signature.");
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            String jti = signedJWT.getJWTClaimsSet().getJWTID();

            if (expirationTime == null || expirationTime.before(new Date()) || invalidatedTokenService.existByJti(jti)) {
                log.warn("[system]: JWT token is expired or invalidated. JTI: {}", jti);
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }

            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("[system]: Failed to parse JWT token: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        } catch (JOSEException e) {
            log.error("[system]: JWT verification failed due to JOSEException: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    public Map<String, Object> getClaimsFromToken(String token) {
        return jwtClaimsSet(token).getClaims();
    }

    public boolean isValidToken(String token) {
        try {
            jwtClaimsSet(token);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    public Date getExpiryTimeFromToken(String token) {
        return jwtClaimsSet(token).getExpirationTime();
    }

    public String getUsernameFromToken(String token) {
        return jwtClaimsSet(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return generateFieldFromToken(token, roleClaim);
    }

    public String getJtiFromToken(String token) {
        return jwtClaimsSet(token).getJWTID();
    }

    private String generateFieldFromToken(String token, String field) {
        JWTClaimsSet claimsSet = jwtClaimsSet(token);
        Object val = claimsSet.getClaim(field);

        if (val == null) {
            log.debug("[system]: Claim '{}' not found in token for subject: {}", field, claimsSet.getSubject());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return String.valueOf(val);
    }
}
