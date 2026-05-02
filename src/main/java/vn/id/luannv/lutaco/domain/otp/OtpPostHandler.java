package vn.id.luannv.lutaco.domain.otp;

import vn.id.luannv.lutaco.enumerate.OtpType;

public interface OtpPostHandler {
    OtpType getSupportedType();

    void handle(String email);
}
