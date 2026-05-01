package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.domain.otp.OtpInfo;
import vn.id.luannv.lutaco.domain.otp.OtpStore;
import vn.id.luannv.lutaco.dto.request.SendOtpRequest;
import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.service.AsyncEmailService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.TimeUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    OtpStore otpStore;
    AsyncEmailService asyncEmailService;

    @Value("${otp.max-attempt}")
    @NonFinal
    int maxAttempts;

    @Value("${app.cache.otp.expire-after-write-minutes}")
    @NonFinal
    int otpExpireAfterWriteMinutes;

    @Value("${otp.cooldown-seconds}")
    @NonFinal
    int cooldownSeconds;

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void sendOtp(SendOtpRequest request, OtpType otpType) {
        String email = request.getEmail();
        String otpCode = generateOtp();
        int maxAttempts = this.maxAttempts;
        OtpInfo otpInfo = new OtpInfo(otpCode, maxAttempts);
        OtpInfo existing = otpStore.get(otpType, email);

        if (existing != null) {
            long elapsed = Duration.between(existing.getCreatedAt(), Instant.now()).getSeconds();

            if (elapsed < cooldownSeconds) {
                throw new BusinessException(
                        ErrorCode.OTP_ALREADY_SENT,
                        Map.of("retryAfterSeconds", cooldownSeconds - elapsed)
                );
            }
        }

        otpStore.save(otpType, email, otpInfo);

        String subject = "Your OTP Code";
        String body = "<p>Your OTP is: <b>" + otpCode
                + "</b></p> OTP will expire in " + otpExpireAfterWriteMinutes + " minutes."
                + TimeUtils.formatToUserZone(otpInfo.getCreatedAt().plusSeconds(otpExpireAfterWriteMinutes * 60L), null, null);
        asyncEmailService.sendEmail(email, subject, body);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void verifyOtp(VerifyOtpRequest request, OtpType otpType) {

        String email = request.getEmail();
        String inputOtp = request.getCode();

        OtpInfo otp = otpStore.get(otpType, email);
        long ttlSeconds = TimeUnit.MINUTES.toSeconds(otpExpireAfterWriteMinutes);

        // 1. Không tồn tại hoặc hết hạn
        if (otp == null || otp.isExpired(ttlSeconds)) {
            otpStore.delete(otpType, email);
            throw new BusinessException(
                    ErrorCode.OTP_EXPIRED,
                    Map.of("email", email)
            );
        }

        // 2. Sai OTP
        if (!otp.isMatch(inputOtp)) {
            otp.increaseAttempt();

            // max attempt
            if (otp.isMaxAttemptsReached()) {
                otpStore.delete(otpType, email);
                throw new BusinessException(
                        ErrorCode.OTP_MAX_ATTEMPT,
                        Map.of(
                                "email", email,
                                "maxAttempts", otp.getMaxAttempts()
                        )
                );
            }

            otpStore.save(otpType, email, otp);

            throw new BusinessException(
                    ErrorCode.OTP_INVALID,
                    Map.of(
                            "attempts", otp.getAttempts(),
                            "remainingAttempts", otp.getMaxAttempts() - otp.getAttempts()
                    )
            );
        }

        // 3. Thành công
        otpStore.delete(otpType, email);
    }

    private String generateOtp() {
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(otp);
    }
}
