package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum OtpType {
    LOGIN("config.enum.otp.type.login"),
    REGISTER("config.enum.otp.type.register"),
    FORGOT_PASSWORD("config.enum.otp.type.forgot_password")
    ;
    String display;
}
