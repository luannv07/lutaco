package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.event.entity.UserRegisteredEvent;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.mapper.UserMapper;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.service.RefreshTokenService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Date;
import java.util.Map;

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
    InvalidatedTokenService invalidatedTokenService;
    RefreshTokenService refreshTokenService;
    OtpService otpService;
    ApplicationEventPublisher applicationEventPublisher;

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
        if (refreshTokenService.findByTokenWithUser(request.getUsername()) != null)
            refreshTokenService.deleteRefreshToken(request.getUsername());

        return AuthenticateResponse.builder()
                .accessToken(jwtAuthenticateService.generateToken(entity))
                .refreshToken(refreshTokenService.createRefreshToken(request.getUsername()).getToken())
                .authenticated(true)
                .build();
    }

    @Override
    @Transactional
    public AuthenticateResponse register(UserCreateRequest request) {
        log.info("AuthServiceImpl create: {}", request.toString());

        if (userRepository.existsByEmail(request.getEmail()))
            throw new BusinessException(ErrorCode.FIELD_EXISTED, Map.of("email", ErrorCode.FIELD_EXISTED.getMessage()));

        if (userRepository.existsByUsername(request.getUsername()))
            throw new BusinessException(ErrorCode.FIELD_EXISTED, Map.of("username", ErrorCode.FIELD_EXISTED.getMessage()));

        UserGender userGender = UserGender.OTHER;

        try {
            userGender = UserGender.valueOf(request.getGender());
        } catch (IllegalArgumentException e) {
            log.info("invalid gender: {}", e.getMessage());
        }

        User entity = userMapper.toEntity(request);
        Role role = roleRepository.findByName(UserType.USER.name())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        entity.setUsername(request.getUsername().toLowerCase());
        entity.setRole(role);
        entity.setUserPlan(UserPlan.FREEMIUM);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setGender(userGender);
        entity.setUserStatus(UserStatus.PENDING_VERIFICATION);
        entity = userRepository.save(entity);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.setAuthenticated(true);

        applicationEventPublisher.publishEvent(
                new UserRegisteredEvent(
                    entity.getUsername(),
                    entity.getEmail(),
                    entity.getId()
                )
        );
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
    public AuthenticateResponse refreshToken(String refreshToken) {
        RefreshToken entity = refreshTokenService.findByToken(refreshToken);
        String username = refreshTokenService.getUsernameByToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENUM_NOT_FOUND));

        if (refreshTokenService.findByTokenWithUser(username) != null)
            refreshTokenService.deleteRefreshToken(username);

        if (entity.getExpiryTime().after(new Date()))
            invalidatedTokenService.addInvalidatedToken(entity.getToken(), entity.getExpiryTime());

        return AuthenticateResponse.builder()
                .refreshToken(refreshTokenService.createRefreshToken(username).getToken())
                .authenticated(true)
                .accessToken(jwtAuthenticateService.generateToken(user))
                .build();
    }
}
