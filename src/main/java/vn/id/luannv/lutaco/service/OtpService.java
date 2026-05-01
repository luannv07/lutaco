package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.SendOtpRequest;
import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.enumerate.OtpType;

public interface OtpService {
    void sendOtp(SendOtpRequest request, OtpType otpType);

    void verifyOtp(VerifyOtpRequest request, OtpType otpType);
}
