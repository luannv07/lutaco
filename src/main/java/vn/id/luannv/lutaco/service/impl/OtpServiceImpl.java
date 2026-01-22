package vn.id.luannv.lutaco.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.entity.Otp;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.OtpRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.AsyncEmailService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.NumberUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    AsyncEmailService emailService;
    OtpRepository otpRepository;
    UserRepository userRepository;

    @NonFinal
    @Value("${otp.expiration-time}")
    long expirationTime;

    @NonFinal
    @Value("${otp.max-delay}")
    long maxDelay;

    @NonFinal
    @Value("${otp.max-resend-count}")
    int maxResendCount;

    @NonFinal
    @Value("${otp.max-attempt}")
    int maxAttempt;

    @NonFinal
    @Value("${otp.max-time-attemp-blocked}")
    long maxTimeAttemptBlocked;

    @PostConstruct
    public void init() {
        expirationTime = expirationTime > 0 ? expirationTime / 60000 : 5;
        maxDelay = maxDelay > 0 ? maxDelay / 60000 : 1;
        maxTimeAttemptBlocked = maxTimeAttemptBlocked > 0 ? maxTimeAttemptBlocked / 60000 : 30;
        maxAttempt = maxAttempt > 0 ? maxAttempt / 60000 : 5;
        maxResendCount = maxResendCount > 0 ? maxResendCount / 60000 : 5;
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void sendOtp(String email, OtpType otpType) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        LocalDateTime now = LocalDateTime.now();

        otpRepository.findSnapshot(user.getId(), otpType).ifPresent(otp -> {
            LocalDateTime lastSendTime =
                    otp.getExpiryTime()
                            .minusMinutes(expirationTime);

            Duration duration = Duration.between(lastSendTime, now);

            if (!duration.isNegative() && duration.compareTo(Duration.ofMinutes(maxDelay)) < 0)
                throw new BusinessException(ErrorCode.OTP_SEND_PREVENT);
        });

        String newCode = NumberUtils.generateOtp();
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(expirationTime);

        otpRepository.insertIfNotExists(
                newCode,
                otpType.name(),
                newExpiry,
                user.getId(),
                maxResendCount,
                maxAttempt
        );

        if (newExpiry.minusMinutes(expirationTime).plusMinutes(maxTimeAttemptBlocked).isBefore(LocalDateTime.now()))
            otpRepository.resetMaxResendCount(user.getId(), otpType.name(), maxResendCount);

        int updated = otpRepository.resendOtp(
                newCode,
                newExpiry,
                user.getId(),
                otpType.name()
        );

        if (updated == 0)
            throw new BusinessException(ErrorCode.OTP_SEND_LIMIT_EXCEEDED);

        String subject = "Mã OTP | " + otpType.name()
                + " | #" + UUID.randomUUID().toString()
                .toUpperCase(Locale.ROOT)
                .replace("-", "");

        String body = """
                <!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','Helvetica Neue',Arial,sans-serif;background-color:#f5f5f5;margin:0;padding:10px}.container{max-width:500px;background:#fff;margin:0 auto;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08)}.header{background:linear-gradient(135deg,#667eea 0,#764ba2 100%);padding:15px 12px;text-align:center}.header h1{margin:0;color:#fff;font-size:22px;font-weight:600}.content{padding:16 12px}.greeting{font-size:16px;color:#333;margin-bottom:20px;line-height:1.4}.otp-box{background:#f8f9fa;border:2px solid #e9ecef;border-radius:8px;padding:24px;text-align:center;margin:24px 0}.otp-label{font-size:13px;color:#6c757d;margin-bottom:12px;font-weight:500}.otp-code{font-size:36px;font-weight:700;color:#667eea;letter-spacing:8px;font-family:'Courier New',monospace}.expiry{background:#fff3cd;border-left:3px solid #ffc107;padding:12px 16px;margin:10px 0;border-radius:4px;font-size:14px;color:#856404}.footer{background:#f8f9fa;padding:10px 24px;text-align:center;border-top:1px solid #e9ecef}.footer p{margin:4px 0;font-size:13px;color:#6c757d}.brand{font-weight:600;color:#667eea}</style></head><body><div class="container"><div class="header"><h1>Xác thực tài khoản</h1></div><div class="content"><p class="greeting">Vui lòng sử dụng mã dưới đây để hoàn tất xác minh:</p><div class="otp-box"><div class="otp-label">MÃ XÁC THỰC</div><div class="otp-code">""" + newCode + """
                </div></div><div class="expiry">⏱️ Mã này sẽ hết hạn vào lúc <strong>""" + newExpiry.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                </strong></div><div class="footer"><p class="brand">LUTACO</p><p>© 2026 Lutaco | Luận & Tuân</p><p style="color:#adb5bd;font-size:12px">Email được gửi vào thời gian: \s""" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """

                </p></div></div></body></html>
                """;

        emailService.sendEmail(user.getEmail(), subject, body);
    }


    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        otpRepository.verifyOtpAtomic(request.getCode(), user.getId(), request.getOtpType().name());

        Otp otp = otpRepository.findSnapshot(user.getId(), request.getOtpType())
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_SEND_FAILED));

        if (otp.getVerifiedAt() != null) {
            user.setUserStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            throw new BusinessException(ErrorCode.OTP_ALREADY_VERIFIED);
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.OTP_EXPIRED);

        if (otp.getMaxAttempt() <= 0)
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPT);

        throw new BusinessException(ErrorCode.OTP_INVALID);
    }

}


