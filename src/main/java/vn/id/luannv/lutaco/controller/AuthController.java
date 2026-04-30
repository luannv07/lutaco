package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.JwtUtils;

import java.util.Date;

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

    @PostMapping("/logout")
    @PreAuthorize("@securityPermission.isLoggedIn()")
            public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest req) {
        String token = JwtUtils.resolveToken(req);
        String jti = jwtService.getJtiFromToken(token);
        Date expiryTime = jwtService.getExpiryTimeFromToken(token);
        authService.logout(jti, expiryTime);

        return ResponseEntity.ok(
                BaseResponse.success("Đăng xuất thành công.")
        );
    }

    @PostMapping("/refresh-token")
            public ResponseEntity<BaseResponse<AuthenticateResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshToken) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        authService.refreshToken(refreshToken.getRefreshToken()),
                        "Làm mới token thành công."
                )
        );
    }

    @PostMapping("/send-otp")
    @PreAuthorize("@securityPermission.isPendingVerification()")
            public ResponseEntity<BaseResponse<Void>> resendOtp(
            @Valid @RequestBody SendOtpRequest request
    ) {
        OtpType otpType = EnumUtils.from(OtpType.class, request.getOtpType());
        otpService.sendOtp(request.getEmail(), otpType, request.getUsername());
        return ResponseEntity.ok(
                BaseResponse.success("Gửi OTP thành công.")
        );
    }

    @PostMapping("/verify-otp")
    @PreAuthorize("@securityPermission.isLoggedIn()")
            public ResponseEntity<BaseResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        otpService.verifyOtp(request);
        return ResponseEntity.ok(
                BaseResponse.success("Xác thực OTP thành công.")
        );
    }
}
