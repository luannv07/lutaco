package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.RefreshTokenRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.JwtUtils;
import vn.id.luannv.lutaco.util.TimeUtils;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;
    JwtService jwtService;
    OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthenticateResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        authService.login(request),
                        "Đăng nhập thành công."
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthenticateResponse>> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        authService.register(request),
                        "Tạo tài khoản thành công."
                ));
    }

    @PostMapping("/register/send-otp")
    @PreAuthorize("@securityPermission.isPendingVerification()")
    public ResponseEntity<BaseResponse<Void>> registerSendOtp(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request);
        String email = jwtService.getEmailClaim(token);

        otpService.sendOtp(email, OtpType.REGISTER);
        return ResponseEntity.ok(
                BaseResponse.success("Gửi OTP thành công.")
        );
    }

    @PostMapping("/register/verify-otp")
    @PreAuthorize("@securityPermission.isLoggedIn()")
    public ResponseEntity<BaseResponse<Void>> registerVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest verifyOtpRequest,
            HttpServletRequest request
    ) {
        String token = JwtUtils.resolveToken(request);
        String email = jwtService.getEmailClaim(token);
        otpService.verifyOtp(verifyOtpRequest, email, OtpType.REGISTER);
        return ResponseEntity.ok(
                BaseResponse.success("Xác thực OTP thành công.")
        );
    }

    @PostMapping("/login/send-otp")
    @PreAuthorize("@securityPermission.isPendingVerification()")
    public ResponseEntity<BaseResponse<Void>> loginSendOtp(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request);
        String email = jwtService.getEmailClaim(token);

        otpService.sendOtp(email, OtpType.LOGIN);
        return ResponseEntity.ok(
                BaseResponse.success("Gửi OTP thành công.")
        );
    }

    @PostMapping("/login/verify-otp")
    @PreAuthorize("@securityPermission.isLoggedIn()")
    public ResponseEntity<BaseResponse<Void>> loginVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest verifyOtpRequest,
            HttpServletRequest request
    ) {
        String token = JwtUtils.resolveToken(request);
        String email = jwtService.getEmailClaim(token);
        otpService.verifyOtp(verifyOtpRequest, email, OtpType.LOGIN);
        return ResponseEntity.ok(
                BaseResponse.success("Xác thực OTP thành công.")
        );
    }

    @PostMapping("/forgot-password/send-otp")
    @PreAuthorize("@securityPermission.isPendingVerification()")
    public ResponseEntity<BaseResponse<Void>> forgotResendOtp(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request);
        String email = jwtService.getEmailClaim(token);

        otpService.sendOtp(email, OtpType.FORGOT_PASSWORD);
        return ResponseEntity.ok(
                BaseResponse.success("Gửi OTP thành công.")
        );
    }

    @PostMapping("/forgot-password/verify-otp")
    @PreAuthorize("@securityPermission.isLoggedIn()")
    public ResponseEntity<BaseResponse<Void>> forgotVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest verifyOtpRequest,
            HttpServletRequest request
    ) {
        String token = JwtUtils.resolveToken(request);
        String email = jwtService.getEmailClaim(token);
        otpService.verifyOtp(verifyOtpRequest, email, OtpType.FORGOT_PASSWORD);
        return ResponseEntity.ok(
                BaseResponse.success("Xác thực OTP thành công.")
        );
    }

    @PostMapping("/logout")
    @PreAuthorize("@securityPermission.isLoggedIn()")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest req) {
        String token = JwtUtils.resolveToken(req);
        String jti = jwtService.getJtiFromToken(token);
        Date expiryTime = jwtService.getExpiryTimeFromToken(token);
        authService.logout(jti, TimeUtils.toInstant(expiryTime));

        return ResponseEntity.ok(
                BaseResponse.success("Đăng xuất thành công.")
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<BaseResponse<AuthenticateResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshToken, HttpServletRequest req) {
        String jti = null;
        Date expiryTime = null;
        try {
            String token = JwtUtils.resolveToken(req);
            jti = jwtService.getJtiFromToken(token);
            expiryTime = jwtService.getExpiryTimeFromToken(token);
        } catch (Exception e) {
            log.info("Token invalidated");
        }
        return ResponseEntity.ok(
                BaseResponse.success(
                        authService.refreshToken(refreshToken, jti, TimeUtils.toInstant(expiryTime)),
                        "Làm mới token thành công."
                )
        );
    }


}
