package vn.id.luannv.lutaco.domain.otp.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.id.luannv.lutaco.domain.otp.OtpPostHandler;
import vn.id.luannv.lutaco.dto.request.UserStatusSetRequest;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.service.WalletService;

@Slf4j
@Component
public class RegisterOtpHandler implements OtpPostHandler {
    UserService userService;
    WalletService walletService;

    @Override
    public OtpType getSupportedType() {
        return OtpType.REGISTER;
    }

    @Override
    public void handle(String email) {
        log.info("Registration OTP handled for email: {}", email);
        UserResponse dto = userService.getByEmail(email);

        UserStatusSetRequest userStatusSetRequest = UserStatusSetRequest.builder()
                .status(UserStatus.ACTIVE.name())
                .build();

        userService.updateStatus(dto.getId(), userStatusSetRequest);
        // xác thực account thành công rồi mới tạo ví
        walletService.createDefaultWallet(dto.getId());
    }
}
