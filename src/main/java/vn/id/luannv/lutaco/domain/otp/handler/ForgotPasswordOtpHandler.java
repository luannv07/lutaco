package vn.id.luannv.lutaco.domain.otp.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.domain.otp.OtpPostHandler;
import vn.id.luannv.lutaco.enumerate.OtpType;

@Slf4j
@Component
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
