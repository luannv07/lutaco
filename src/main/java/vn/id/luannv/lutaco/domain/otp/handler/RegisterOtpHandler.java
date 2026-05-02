package vn.id.luannv.lutaco.domain.otp.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.domain.otp.OtpPostHandler;
import vn.id.luannv.lutaco.enumerate.OtpType;

@Slf4j
@Component
public class RegisterOtpHandler implements OtpPostHandler {
    @Override
    public OtpType getSupportedType() {
        return OtpType.REGISTER;
    }

    @Override
    public Object handle(String email) {
        // No additional handling needed for registration OTP
        log.info("Registration OTP handled for email: {}", email);
        return null;
    }
}
