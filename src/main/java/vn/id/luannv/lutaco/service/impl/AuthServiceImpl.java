package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.MasterDictionaryDto;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.MasterDictionaryType;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.mapper.UserMapper;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.*;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    JwtService jwtAuthenticateService;
    MasterDictionaryService masterDictionaryService;
    InvalidatedTokenService invalidatedTokenService;
    RefreshTokenService refreshTokenService;
    OtpService otpService;

    @Override
    public AuthenticateResponse login(LoginRequest request) {
        log.info("AuthServiceImpl login: {}", request.toString());

        User entity = userRepository.findByUsername(request.getUsername()).orElseThrow(() ->
                new BusinessException(ErrorCode.LOGIN_FAILED));
        // ko match encoded password | banned | disabled
        if (!passwordEncoder.matches(request.getPassword(), entity.getPassword())
                || entity.getUserStatus().equals(UserStatus.DISABLED_BY_USER)
                || entity.getUserStatus().equals(UserStatus.BANNED) )

            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        if (refreshTokenService.findByTokenWithUser(request.getUsername()) != null) {
            log.info("refreshTokenService.findByTokenWithUser(request.getUsername()): {}", refreshTokenService.findByTokenWithUser(request.getUsername()));
            refreshTokenService.deleteRefreshToken(request.getUsername());
        }

        return AuthenticateResponse.builder()
                .accessToken(jwtAuthenticateService.generateToken(entity))
                .refreshToken(refreshTokenService.createRefreshToken(request.getUsername()).getToken())
                .authenticated(true)
                .build();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public AuthenticateResponse register(UserCreateRequest request) {
        log.info("AuthServiceImpl create: {}", request.toString());

        if (userRepository.existsByEmail(request.getEmail()))
            throw new BusinessException(ErrorCode.FIELD_EXISTS, Map.of("email", ErrorCode.FIELD_EXISTS.getMessage()));

        if (userRepository.existsByUsername(request.getUsername()))
            throw new BusinessException(ErrorCode.FIELD_EXISTS, Map.of("username", ErrorCode.FIELD_EXISTS.getMessage()));

        MasterDictionaryDto dictionaryDto = masterDictionaryService.getByCategoryAndCode(MasterDictionaryType.GENDER.name(), request.getGender());

        User entity = userMapper.toEntity(request);
        Role role = roleRepository.findByName("USER").get();

        entity.setRole(role);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setGender(dictionaryDto.getCode());
        entity.setUserStatus(UserStatus.PENDING_VERIFICATION);
        entity = userRepository.save(entity);

        otpService.sendOtp(entity.getEmail(), OtpType.REGISTER);

        return AuthenticateResponse.builder()
                .authenticated(true)
                .accessToken(jwtAuthenticateService.generateToken(entity))
                .refreshToken(refreshTokenService.createRefreshToken(request.getUsername()).getToken())
                .build();
    }


    @Override
    public void logout(String jti, Date expiryTime) {
        invalidatedTokenService.addInvalidatedToken(jti, expiryTime);
        refreshTokenService.deleteRefreshToken(SecurityUtils.getCurrentUsername());
    }

    @Override
    public AuthenticateResponse refreshToken(String username, String jti, Date expiryTime) {

        User entity = userRepository.findByUsername(username).orElseThrow(() ->
                new BusinessException(ErrorCode.UNAUTHORIZED));

        if (refreshTokenService.findByTokenWithUser(username) != null)
            refreshTokenService.deleteRefreshToken(username);

//        if (expiryTime.after(new Date()))
//            invalidatedTokenService.addInvalidatedToken(jti, expiryTime);

        return AuthenticateResponse.builder()
                .refreshToken(refreshTokenService.createRefreshToken(username).getToken())
                .authenticated(true)
                .accessToken(jwtAuthenticateService.generateToken(entity))
                .build();
    }
}
