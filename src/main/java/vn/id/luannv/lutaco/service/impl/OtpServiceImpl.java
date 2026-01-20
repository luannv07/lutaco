package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
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
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.util.DateTimeFormatUtils;
import vn.id.luannv.lutaco.util.NumberUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
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
    int maxTimeAttemptBlocked;

    @Override
    @Transactional
    public void sendOtp(String email, OtpType otpType) {
        log.error("Chay duoc vao day ko?");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Otp otpEntity = otpRepository.getOtpsByUserAndOtpType(user, otpType);

        if (otpEntity != null) {
            if (otpEntity.getVerifiedAt() != null)
                throw new BusinessException(ErrorCode.OTP_ALREADY_VERIFIED);

            if (otpEntity.getMaxResendCount() <= 0 ||
                    otpEntity.getExpiryTime()
                            .plusMinutes(maxTimeAttemptBlocked)
                            .plusMinutes(expirationTime)
                            .isBefore(LocalDateTime.now()))
                throw new BusinessException(ErrorCode.OTP_SEND_LIMIT_EXCEEDED);

            otpEntity.setMaxResendCount(
                            otpEntity.getMaxResendCount() - 1 >= 0
                                    ? otpEntity.getMaxResendCount() - 1
                                    : maxResendCount
            );

            if (otpEntity.getExpiryTime().plusMinutes(-1 * expirationTime / 60000)
                    .plusMinutes(maxDelay / 60000)
                    .isAfter(LocalDateTime.now()))
                throw new BusinessException(ErrorCode.OTP_SEND_PREVENT);

        } else {
            otpEntity = Otp.builder()
                    .user(user)
                    .otpType(otpType)
                    .maxResendCount(maxResendCount)
                    .maxAttempt(maxAttempt)
                    .build();
        }
        otpEntity.setCode(NumberUtils.generateOtp());
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(expirationTime / 60000));
        otpRepository.save(otpEntity);

        String subject = "Mã OTP" + " | " + otpType.name() + " | #" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT).replaceAll("-", "");

        String body = """
                <!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','Helvetica Neue',Arial,sans-serif;background-color:#f5f5f5;margin:0;padding:10px}.container{max-width:500px;background:#fff;margin:0 auto;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08)}.header{background:linear-gradient(135deg,#667eea 0,#764ba2 100%);padding:15px 12px;text-align:center}.header h1{margin:0;color:#fff;font-size:22px;font-weight:600}.content{padding:16 12px}.greeting{font-size:16px;color:#333;margin-bottom:20px;line-height:1.4}.otp-box{background:#f8f9fa;border:2px solid #e9ecef;border-radius:8px;padding:24px;text-align:center;margin:24px 0}.otp-label{font-size:13px;color:#6c757d;margin-bottom:12px;font-weight:500}.otp-code{font-size:36px;font-weight:700;color:#667eea;letter-spacing:8px;font-family:'Courier New',monospace}.expiry{background:#fff3cd;border-left:3px solid #ffc107;padding:12px 16px;margin:10px 0;border-radius:4px;font-size:14px;color:#856404}.footer{background:#f8f9fa;padding:10px 24px;text-align:center;border-top:1px solid #e9ecef}.footer p{margin:4px 0;font-size:13px;color:#6c757d}.brand{font-weight:600;color:#667eea}</style></head><body><div class="container"><div class="header"><h1>Xác thực tài khoản</h1></div><div class="content"><p class="greeting">Vui lòng sử dụng mã dưới đây để hoàn tất xác minh:</p><div class="otp-box"><div class="otp-label">MÃ XÁC THỰC</div><div class="otp-code">""" + otpEntity.getCode() + """
                    </div></div><div class="expiry">⏱️ Mã này sẽ hết hạn vào lúc <strong>""" + otpEntity.getExpiryTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                </strong></div><div class="footer"><p class="brand">LUTACO</p><p>© 2026 Lutaco | Luận & Tuân</p><p style="color:#adb5bd;font-size:12px">Email được gửi vào thời gian: \s""" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                
                </p></div></div></body></html>
                """;


        emailService.sendEmail(user.getEmail(), subject, body);

    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Otp otpEntity = otpRepository.getOtpsByUserAndOtpType(user, request.getOtpType());
        if (otpEntity == null)
            throw new BusinessException(ErrorCode.OTP_SEND_FAILED);

        if (otpEntity.getVerifiedAt() != null)
            throw new BusinessException(ErrorCode.OTP_ALREADY_VERIFIED);

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.OTP_EXPIRED);

        if (otpEntity.getMaxAttempt() <= 0)
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPT);

        otpEntity.setMaxAttempt(otpEntity.getMaxAttempt() - 1);

        if (!otpEntity.getCode().equals(request.getCode())) {
            otpRepository.save(otpEntity);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        otpEntity.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otpEntity);

        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
