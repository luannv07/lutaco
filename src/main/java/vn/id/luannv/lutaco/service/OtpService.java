package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.OtpType;

import java.util.Date;

public interface OtpService {
    void sendOtp(String email, OtpType otpType);

    void verifyOtp(VerifyOtpRequest request);
}
