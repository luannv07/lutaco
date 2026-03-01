package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.UserMapper;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenService invalidatedTokenService;

    @Override
    public UserResponse create(UserCreateRequest request) {
        log.warn("[system]: Attempted to create user via create method, which is not supported. Use AuthServiceImpl.register instead.");
        return null;
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getDetail(String id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Fetching details for user with ID: {}", username, id);
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("[{}]: User with ID {} not found.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage()));
                });
    }

    @Override
    public Page<UserResponse> search(UserFilterRequest request, Integer page, Integer size) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Searching users with filter: {}, page: {}, size: {}.", username, request, page, size);
        Pageable pageable = PageRequest.of(page - 1, size);

        try {
            request.setUserPlan(EnumUtils.from(UserPlan.class, request.getUserPlan()).name());
            request.setUserStatus(EnumUtils.from(UserStatus.class, request.getUserStatus()).name());
        } catch (Exception e) {
            request.setUserPlan(null);
            request.setUserStatus(null);
            log.info("[{}]: {}", username, e.getMessage());
        }

        Page<UserResponse> result = userRepository.findByFilters(request, pageable)
                .map(userMapper::toResponse);
        log.info("[{}]: Found {} users matching the criteria.", username, result.getTotalElements());
        return result;
    }

    @Override
    public UserResponse update(String id, UserCreateRequest request) {
        log.warn("[system]: Attempted to update user via update method, which is not supported. Use updateUser instead.");
        return null;
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Updating user with ID: {} with data: {}", username, id, request);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: User with ID {} not found for update.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage()));
                });
        UserGender userGender = user.getGender();

        try {
            userGender = UserGender.valueOf(request.getGender());
        } catch (IllegalArgumentException e) {
            log.warn("[{}]: Invalid gender '{}' provided for user ID {}. Keeping existing gender.", username, request.getGender(), id);
        }

        userMapper.updateUser(request, user);
        user.setGender(userGender);
        User saved = userRepository.save(user);
        log.info("[{}]: User with ID {} updated successfully.", username, id);
        return userMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void updateStatus(String id, UserStatusSetRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        log.info("[{}]: {}, {}", username, SecurityUtils.getCurrentRoleName(), UserType.USER.name());
        if (SecurityUtils.getCurrentRoleName().equals(UserType.USER.name())) {
            user.setUserStatus(UserStatus.DISABLED_BY_USER);
            return;
        }
        user.setUserStatus(EnumUtils.from(UserStatus.class, request.getStatus()));
        userRepository.save(user);
        log.info("[{}]: User status updated to: {}", username, user.getUserStatus());
    }

    @Override
    public void deleteById(String id) {
        log.warn("[system]: Attempted to delete user by ID {}, which is not supported. Use updateStatus to disable/ban.", id);
        throw new UnsupportedOperationException("Direct deletion of users is not supported. Use updateStatus to disable or ban.");
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void updateUserRole(String id, UserRoleRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        user.setRole(role);
        userRepository.save(user);
        log.info("[{}]: Updated role for user {} to {}", username, id, request.getRoleName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void updatePassword(String id, UpdatePasswordRequest request, String jti, Date expiryTime) {
        String username = SecurityUtils.getCurrentUsername();
        if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        String currentRoleName = SecurityUtils.getCurrentRoleName();

        if (currentRoleName.equals(UserType.SYS_ADMIN.name()) || currentRoleName.equals(UserType.ADMIN.name())
                || (currentRoleName.equals(UserType.USER.name()) && passwordEncoder.matches(request.getOldPassword(), user.getPassword()))) {
            user.setPassword(request.getNewPassword());
            userRepository.save(user);
            invalidatedTokenService.addInvalidatedToken(jti, expiryTime);
            log.info("[{}]: Password updated for user {}", username, id);
            return;
        }
        log.warn("[{}]: Password update failed for user {}. Operation not allowed.", username, id);
        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
    }
}
