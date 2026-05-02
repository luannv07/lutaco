package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.RefreshTokenRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;
import vn.id.luannv.lutaco.service.RefreshTokenService;
import vn.id.luannv.lutaco.service.RoleService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.Instant;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RefreshTokenService refreshTokenService;
    JwtService jwtService;
    RoleService roleService;
    InvalidatedTokenService invalidatedTokenService;

    @Override
    @Transactional
    public AuthenticateResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())
                || user.getUserStatus() == UserStatus.INACTIVE
                || user.getUserStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        refreshTokenService.deleteAllByUsername(user.getUsername());
        return AuthenticateResponse.builder()
                .accessToken(jwtService.generateToken(
                        user.getUsername(),
                        user.getRole().getCode().name(),
                        user.getEmail()
                )).refreshToken(refreshTokenService.createRefreshToken(user.getId()).getRefToken())
                .build();
    }

    @Override
    @Transactional
    public AuthenticateResponse register(UserCreateRequest request) {
        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new BusinessException(ErrorCode.ENTITY_EXISTED);
        }

        UserGender gender = EnumUtils.from(UserGender.class, request.getGender());

        Role role = roleService.getByRoleCode(UserType.USER);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setGender(gender);
        user.setUserStatus(UserStatus.PENDING);
        user.setUserPlan(UserPlan.FREEMIUM);
        user.setRole(role);

        userRepository.save(user);
        return AuthenticateResponse.builder()
                .accessToken(jwtService.generateToken(
                        user.getUsername(),
                        user.getRole().getCode().name(),
                        user.getEmail()
                )).refreshToken(refreshTokenService.createRefreshToken(user.getId()).getRefToken())
                .build();
    }


    @Override
    public void logout(String jti, Instant expiryTime) {
        invalidatedTokenService.addInvalidatedToken(jti, expiryTime);
        refreshTokenService.deleteAllByUsername(SecurityUtils.getCurrentUsername());
    }

    @Override
    @Transactional
    public AuthenticateResponse refreshToken(RefreshTokenRequest request, String jti, Instant expiryTime) {
        Instant now = Instant.now();

        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());

        if (now.isAfter(refreshToken.getExpiryTime()))
            throw new BusinessException(ErrorCode.UNAUTHORIZED);

        int updated = refreshTokenService.updateStatusUsedByRefToken(request.getRefreshToken());

        if (updated == 0)
            throw new BusinessException(ErrorCode.UNAUTHORIZED);

        if (jti != null && expiryTime != null && now.isBefore(expiryTime))
            invalidatedTokenService.addInvalidatedToken(jti, expiryTime);

        User user = refreshToken.getUser();

        return AuthenticateResponse.builder()
                .accessToken(jwtService.generateToken(
                        user.getUsername(),
                        user.getRole().getCode().name(),
                        user.getEmail()
                )).refreshToken(refreshTokenService.createRefreshToken(user.getId()).getRefToken())
                .build();
    }
}
