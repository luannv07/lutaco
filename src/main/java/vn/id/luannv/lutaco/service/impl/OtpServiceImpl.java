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
import vn.id.luannv.lutaco.util.EnumUtils;
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
        log.info("OTP service initialized with expirationTime: {} min, maxDelay: {} min, maxResendCount: {}, maxAttempt: {}, maxTimeAttemptBlocked: {} min.",
                expirationTime, maxDelay, maxResendCount, maxAttempt, maxTimeAttemptBlocked);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void sendOtp(String email, OtpType otpType) {
        log.info("Attempting to send OTP of type {} to email: {}", otpType, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User with email {} not found for OTP send.", email);
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        if (otpType != OtpType.FORGOT_PASSWORD && !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            log.warn("Attempt to send OTP of type {} to email {} failed: User not authenticated.", otpType, email);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        LocalDateTime now = LocalDateTime.now();

        otpRepository.findSnapshot(user.getId(), otpType).ifPresent(otp -> {
            LocalDateTime lastSendTime = otp.getExpiryTime().minusMinutes(expirationTime);
            Duration duration = Duration.between(lastSendTime, now);

            if (!duration.isNegative() && duration.compareTo(Duration.ofMinutes(maxDelay)) < 0) {
                log.warn("OTP send prevented for user {} (email: {}): Too soon since last send. Remaining delay: {} seconds.", user.getId(), email, Duration.ofMinutes(maxDelay).minus(duration).getSeconds());
                throw new BusinessException(ErrorCode.OTP_SEND_PREVENT);
            }
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

        if (affected == 0) {
            log.debug("OTP record already exists for user {} and OTP type {}. Attempting to resend or update.", user.getId(), otpType);
            if (newExpiry.minusMinutes(expirationTime).plusMinutes(maxTimeAttemptBlocked).isBefore(LocalDateTime.now())) {
                log.info("Resetting max resend count for user {} and OTP type {} due to blocked time expiration.", user.getId(), otpType);
                otpRepository.resetMaxResendCount(user.getId(), otpType.name(), maxResendCount);
            }

            int updated = otpRepository.resendOtp(
                    newCode,
                    newExpiry,
                    user.getId(),
                    otpType.name()
            );

            if (updated == 0) {
                log.warn("OTP resend limit exceeded for user {} and OTP type {}.", user.getId(), otpType);
                throw new BusinessException(ErrorCode.OTP_SEND_LIMIT_EXCEEDED);
            }
            log.info("OTP resent for user {} (email: {}). New code: {}, New expiry: {}.", user.getId(), email, newCode, newExpiry);
        } else {
            log.info("New OTP generated and saved for user {} (email: {}). Code: {}, Expiry: {}.", user.getId(), email, newCode, newExpiry);
        }

        EmailTemplateService.EmailFields fields = EmailTemplateService.getOtpTemplate(email, otpType, newCode, newExpiry);
        emailService.sendEmail(user.getEmail(), fields.subject(), fields.body());
        log.info("Email with OTP sent to {}.", email);
    }


    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void verifyOtp(VerifyOtpRequest request) {
        log.info("Attempting to verify OTP of type {} for email: {}", request.getOtpType(), request.getEmail());
        OtpType otpType = EnumUtils.from(OtpType.class, request.getOtpType());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User with email {} not found for OTP verification.", request.getEmail());
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        if (!user.getUsername().equalsIgnoreCase(SecurityUtils.getCurrentUsername())) {
            log.warn("User {} attempted to verify OTP for email {} which does not match their authenticated username.", SecurityUtils.getCurrentUsername(), request.getEmail());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Otp otp = otpRepository.findSnapshot(user.getId(), otpType)
                .orElseThrow(() -> {
                    log.warn("No OTP record found for user {} and OTP type {}.", user.getId(), otpType);
                    return new BusinessException(ErrorCode.OTP_SEND_FAILED);
                });
        final int initialAttemptCount = otp.getMaxAttempt();

        otpRepository.verifyOtpAtomic(request.getCode(), user.getId(), otpType.name());

        Otp afterQuery = otpRepository.findSnapshot(user.getId(), otpType)
                .orElseThrow(() -> {
                    log.error("OTP record disappeared after atomic verification attempt for user {} and OTP type {}.", user.getId(), otpType);
                    return new BusinessException(ErrorCode.OTP_SEND_FAILED);
                });

        if (initialAttemptCount == afterQuery.getMaxAttempt() && afterQuery.getMaxAttempt() != 0 && afterQuery.getVerifiedAt() != null) {
            user.setUserStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            log.info("OTP successfully verified for user {} (email: {}). User status set to ACTIVE.", user.getId(), request.getEmail());
            return;
        }

        if (otp.getVerifiedAt() != null) {
            log.warn("OTP for user {} and type {} already verified.", user.getId(), otpType);
            throw new BusinessException(ErrorCode.OTP_ALREADY_VERIFIED);
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("OTP for user {} and type {} has expired. Expiry time: {}.", user.getId(), otpType, otp.getExpiryTime());
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        if (otp.getMaxAttempt() <= 0) {
            log.warn("OTP max attempt limit reached for user {} and type {}.", user.getId(), otpType);
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPT);
        }

        log.warn("Invalid OTP code provided for user {} and type {}. Code: {}. Remaining attempts: {}.", user.getId(), otpType, request.getCode(), otp.getMaxAttempt() - 1);
        throw new BusinessException(ErrorCode.OTP_INVALID);
    }
}
