package vn.id.luannv.lutaco.domain.otp.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.domain.otp.OtpPostHandler;
import vn.id.luannv.lutaco.enumerate.OtpType;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ForgotPasswordOtpHandler implements OtpPostHandler {
    @Override
    public OtpType getSupportedType() {
        return OtpType.FORGOT_PASSWORD;
    }

    @Override
    public void handle(String email) {
        // No additional handling needed for forgot password OTP
        log.info("Forgot password OTP handled for email: {}", email);
    }
}
