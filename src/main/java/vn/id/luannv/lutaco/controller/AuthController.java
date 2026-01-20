package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.SendOtpRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.VerifyOtpRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;
import vn.id.luannv.lutaco.service.OtpService;
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
    public ResponseEntity<BaseResponse<AuthenticateResponse>> login(@Valid @RequestBody LoginRequest request) {
        return  ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        authService.login(request),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @Operation(summary = "Tạo người dùng mới", description = "Tạo một người dùng mới với thông tin được cung cấp")
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthenticateResponse>> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        authService.register(request),
                        MessageKeyConst.Success.CREATED
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest req) {
        String token = JwtUtils.resolveToken(req);

        String jti = jwtService.getJtiFromToken(token);
        Date expiryTime = jwtService.getExpiryTimeFromToken(token);

        authService.logout(jti, expiryTime);

        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        null,
                        MessageKeyConst.Success.SUCCESS
                )
        );

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<BaseResponse<AuthenticateResponse>> refreshToken(HttpServletRequest req) {
        String token = JwtUtils.resolveToken(req);

        String username =  jwtService.getUsernameFromToken(token);
        String jti = jwtService.getJtiFromToken(token);
        Date expiryTime = jwtService.getExpiryTimeFromToken(token);

        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        authService.refreshToken(username, jti, expiryTime),
                        MessageKeyConst.Success.SUCCESS
                )
        );
    }

    @Operation(
            summary = "API dùng để send/resend OTP, có giới hạn số lần yêu cầu"
    )
    @PostMapping("/send-otp")
    public ResponseEntity<BaseResponse<Void>> resendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.getEmail(), request.getOtpType());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        null,
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<BaseResponse<Void>> resendOtp(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        null,
                        MessageKeyConst.Success.SENT
                )
        );
    }
}
