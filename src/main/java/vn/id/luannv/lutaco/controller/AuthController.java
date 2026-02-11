package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.JwtUtils;

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
    @Operation(
            summary = "Đăng nhập",
            description = "Xác thực người dùng bằng thông tin đăng nhập hợp lệ và trả về access token cùng refresh token"
    )
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
    @Operation(
            summary = "Đăng ký người dùng",
            description = "Tạo tài khoản người dùng mới và trả về thông tin xác thực sau khi đăng ký thành công"
    )
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
    @Operation(
            summary = "Đăng xuất",
            description = "Huỷ hiệu lực của JWT hiện tại bằng cách đưa token vào blacklist"
    )
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
    @Operation(
            summary = "Làm mới access token",
            description = "Cấp access token mới dựa trên token hiện tại còn hiệu lực"
    )
    public ResponseEntity<BaseResponse<AuthenticateResponse>> refreshToken( @Valid @RequestBody RefreshTokenRequest refreshToken) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        authService.refreshToken(refreshToken.getRefreshToken()),
                        "Làm mới token thành công."
                )
        );
    }

    @PostMapping("/send-otp")
    @PreAuthorize("@securityPermission.isPendingVerification()")
    @Operation(
            summary = "Gửi hoặc gửi lại OTP",
            description = "Gửi mã OTP theo loại yêu cầu, có giới hạn số lần gửi trong một khoảng thời gian"
    )
    public ResponseEntity<BaseResponse<Void>> resendOtp(
            @Valid @RequestBody SendOtpRequest request
    ) {
        OtpType otpType = OtpType.of(request.getOtpType());
        otpService.sendOtp(request.getEmail(), otpType);
        return ResponseEntity.ok(
                BaseResponse.success("Gửi OTP thành công.")
        );
    }

    @PostMapping("/verify-otp")
    @PreAuthorize("@securityPermission.isLoggedIn()")
    @Operation(
            summary = "Xác thực OTP",
            description = "Xác thực mã OTP người dùng đã nhận để hoàn tất bước xác minh tài khoản"
    )
    public ResponseEntity<BaseResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        otpService.verifyOtp(request);
        return ResponseEntity.ok(
                BaseResponse.success("Xác thực OTP thành công.")
        );
    }
}
