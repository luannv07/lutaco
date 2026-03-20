package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.enumerate.OtpType;

public interface OtpService {
    void sendOtp(String email, OtpType otpType, String username);

    void verifyOtp(VerifyOtpRequest request);
}
