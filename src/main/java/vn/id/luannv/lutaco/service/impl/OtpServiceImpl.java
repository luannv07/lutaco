package vn.id.luannv.lutaco.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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
import vn.id.luannv.lutaco.service.EmailTemplateService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.RandomUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.Duration;
import java.time.LocalDateTime;

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
        maxAttempt = maxAttempt > 0 ? maxAttempt : 5;
        maxResendCount = maxResendCount > 0 ? maxResendCount : 5;
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void sendOtp(String email, OtpType otpType) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (otpType != OtpType.FORGOT_PASSWORD)
            if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
                throw new BusinessException(ErrorCode.FORBIDDEN);

        LocalDateTime now = LocalDateTime.now();

        otpRepository.findSnapshot(user.getId(), otpType).ifPresent(otp -> {
            LocalDateTime lastSendTime =
                    otp.getExpiryTime()
                            .minusMinutes(expirationTime);

            Duration duration = Duration.between(lastSendTime, now);

            if (!duration.isNegative() && duration.compareTo(Duration.ofMinutes(maxDelay)) < 0)
                throw new BusinessException(ErrorCode.OTP_SEND_PREVENT);
        });

        String newCode = RandomUtils.generateOtp();
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(expirationTime);

        int affected = otpRepository.insertIfNotExists(
                newCode,
                otpType.name(),
                newExpiry,
                user.getId(),
                maxResendCount,
                maxAttempt
        );

        log.info("affected: {}", affected);

        if (affected == 0) {
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
        }


        EmailTemplateService.EmailFields fields = EmailTemplateService.getOtpTemplate(email, otpType, newCode, newExpiry);

        emailService.sendEmail(user.getEmail(), fields.subject(), fields.body());
    }


    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void verifyOtp(VerifyOtpRequest request) {
        OtpType otpType = OtpType.of(request.getOtpType());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!user.getUsername().equalsIgnoreCase(SecurityUtils.getCurrentUsername()))
            throw new BusinessException(ErrorCode.FORBIDDEN);

        Otp otp = otpRepository.findSnapshot(user.getId(), otpType)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_SEND_FAILED));
        final int attempt = otp.getMaxAttempt();

        otpRepository.verifyOtpAtomic(request.getCode(), user.getId(), otpType.name());

        Otp afterQuery = otpRepository.findSnapshot(user.getId(), otpType)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_SEND_FAILED));

        log.info("attempt and after query attempt: {} {}", attempt, afterQuery.getMaxAttempt());
        if (attempt == afterQuery.getMaxAttempt() && afterQuery.getMaxAttempt() != 0 && afterQuery.getVerifiedAt() != null) {
            user.setUserStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            return;
        }

        if (otp.getVerifiedAt() != null) {
            throw new BusinessException(ErrorCode.OTP_ALREADY_VERIFIED);
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.OTP_EXPIRED);

        if (otp.getMaxAttempt() <= 0)
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPT);

        throw new BusinessException(ErrorCode.OTP_INVALID);
    }

}


