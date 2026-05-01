package vn.id.luannv.lutaco.domain.otp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.time.Instant;

@Getter
@ToString(exclude = "code")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpInfo {

    String code;
    int maxAttempts;
    Instant createdAt;
    @NonFinal
    int attempts;

    public OtpInfo(String code, int maxAttempts) {
        this.code = code;
        this.maxAttempts = maxAttempts;
        this.attempts = 0;
        this.createdAt = Instant.now();
    }

    public boolean isMatch(String input) {
        return this.code.equals(input);
    }

    public void increaseAttempt() {
        this.attempts++;
    }

    public boolean isMaxAttemptsReached() {
        return attempts >= maxAttempts;
    }

    public boolean isExpired(long ttlSeconds) {
        return Instant.now().isAfter(createdAt.plusSeconds(ttlSeconds));
    }
}