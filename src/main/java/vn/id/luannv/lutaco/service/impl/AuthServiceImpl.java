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
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public AuthenticateResponse login(LoginRequest request) {
        log.info("[unknown]: Attempting to log in user: {}", request.getUsername());

        User entity = userRepository.findByUsername(request.getUsername().toLowerCase()).orElseThrow(() -> {
            log.warn("[unknown]: Login failed for user {}: User not found.", request.getUsername());
            return new BusinessException(ErrorCode.LOGIN_FAILED);
        });

        if (!passwordEncoder.matches(request.getPassword(), entity.getPassword())) {
            log.warn("[unknown]: Login failed for user {}: Invalid credentials.", request.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        if (entity.getUserStatus().equals(UserStatus.DISABLED_BY_USER) || entity.getUserStatus().equals(UserStatus.BANNED)) {
            log.warn("[unknown]: Login failed for user {}: Account is {}.", request.getUsername(), entity.getUserStatus());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        if (refreshTokenService.findByTokenWithUser(request.getUsername()) != null) {
            log.debug("[unknown]: Existing refresh token found for user {}, deleting it.", request.getUsername());
            refreshTokenService.deleteRefreshToken(request.getUsername());
        }

        AuthenticateResponse response = AuthenticateResponse.builder()
                .accessToken(jwtAuthenticateService.generateToken(entity))
                .refreshToken(refreshTokenService.createRefreshToken(request.getUsername()).getToken())
                .authenticated(true)
                .build();
        log.info("[unknown]: User {} logged in successfully.", request.getUsername());
        return response;
    }

    @Override
    @Transactional
    public AuthenticateResponse register(UserCreateRequest request) {
        log.info("[unknown]: Attempting to register new user with username: {} and email: {}", request.getUsername(), request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[unknown]: User registration failed: Email {} already exists.", request.getEmail());
            throw new BusinessException(ErrorCode.FIELD_EXISTED, Map.of("email", ErrorCode.FIELD_EXISTED.getMessage()));
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("[unknown]: User registration failed: Username {} already exists.", request.getUsername());
            throw new BusinessException(ErrorCode.FIELD_EXISTED, Map.of("username", ErrorCode.FIELD_EXISTED.getMessage()));
        }

        UserGender userGender = UserGender.OTHER;
        try {
            userGender = UserGender.valueOf(request.getGender());
        } catch (IllegalArgumentException e) {
            log.warn("[unknown]: Invalid gender '{}' provided during registration for user {}. Defaulting to OTHER.", request.getGender(), request.getUsername());
        }

        User entity = userMapper.toEntity(request);
        Role role = roleRepository.findByName(UserType.USER.name())
                .orElseThrow(() -> {
                    log.error("[system]: Role '{}' not found in the system during user registration.", UserType.USER.name());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        entity.setUsername(request.getUsername().toLowerCase());
        entity.setRole(role);
        entity.setUserPlan(UserPlan.FREEMIUM);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setGender(userGender);
        entity.setUserStatus(UserStatus.PENDING_VERIFICATION);
        entity = userRepository.save(entity);
        log.info("[unknown]: New user {} (ID: {}) registered successfully with status PENDING_VERIFICATION.", entity.getUsername(), entity.getId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.setAuthenticated(true);

        applicationEventPublisher.publishEvent(
                new UserRegisteredEvent(
                        entity.getUsername(),
                        entity.getEmail(),
                        entity.getId()
                )
        );
        log.debug("[unknown]: UserRegisteredEvent published for user ID: {}", entity.getId());

        return AuthenticateResponse.builder()
                .authenticated(true)
                .accessToken(jwtAuthenticateService.generateToken(entity))
                .refreshToken(refreshTokenService.createRefreshToken(request.getUsername()).getToken())
                .build();
    }


    @Override
    public void logout(String jti, Date expiryTime) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: User logging out. Invalidating token JTI: {}", username, jti);
        invalidatedTokenService.addInvalidatedToken(jti, expiryTime);
        refreshTokenService.deleteRefreshToken(username);
        log.info("[{}]: User successfully logged out.", username);
    }

    @Override
    public AuthenticateResponse refreshToken(String refreshToken) {
        RefreshToken entity = refreshTokenService.findByToken(refreshToken);
        String tokenUsername = refreshTokenService.getUsernameByToken(refreshToken);
        User user = userRepository.findByUsername(tokenUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENUM_NOT_FOUND));

        if (refreshTokenService.findByTokenWithUser(tokenUsername) != null) {
            refreshTokenService.deleteRefreshToken(tokenUsername);
        }

        if (entity.getExpiryTime().after(new Date())) {
            invalidatedTokenService.addInvalidatedToken(entity.getToken(), entity.getExpiryTime());
        }

        return AuthenticateResponse.builder()
                .refreshToken(refreshTokenService.createRefreshToken(tokenUsername).getToken())
                .authenticated(true)
                .accessToken(jwtAuthenticateService.generateToken(user))
                .build();
    }
}
