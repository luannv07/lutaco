package vn.id.luannv.lutaco.infra.cache.otp;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.domain.otp.OtpInfo;
import vn.id.luannv.lutaco.domain.otp.OtpStore;
import vn.id.luannv.lutaco.enumerate.OtpType;

import java.util.concurrent.TimeUnit;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class CaffeineOtpStore implements OtpStore {
    Cache<String, OtpInfo> otpCache;

    @Override
    public void save(OtpType type, String identifier, OtpInfo otp) {
        String key = buildKey(type, identifier);
        log.debug("Saving OTP for key: {}", key);
        otpCache.put(key, otp);
    }

    @Override
    public OtpInfo get(OtpType type, String identifier) {
        String key = buildKey(type, identifier);
        log.debug("Getting OTP for key: {}", key);
        return otpCache.getIfPresent(key);
    }

    @Override
    public void delete(OtpType type, String identifier) {
        String key = buildKey(type, identifier);
        log.debug("Deleting OTP for key: {}", key);
        otpCache.invalidate(key);
    }

    private String buildKey(OtpType type, String identifier) {
        return "otp:" + type.name() + ":" + hash(identifier);
    }

    private String hash(String input) {
        return Integer.toHexString(input.hashCode()); // đơn giản, hoặc dùng SHA-256
    }
}
