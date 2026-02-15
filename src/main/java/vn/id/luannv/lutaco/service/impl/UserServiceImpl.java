package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.UserMapper;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.util.SecurityUtils;

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

    @Override
    public UserResponse create(UserCreateRequest request) {
        log.warn("Attempted to create user via create method, which is not supported. Use AuthServiceImpl.register instead.");
        return null;
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getDetail(String id) {
        log.info("Fetching details for user with ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage()));
                });
    }

    @Override
    @Cacheable(value = "users", key = "{#request, #page, #size}")
    public Page<UserResponse> search(UserFilterRequest request, Integer page, Integer size) {
        log.info("Searching users with filter: {}, page: {}, size: {}.", request, page, size);
        Pageable pageable = PageRequest.of(page - 1, size);

        if (request.getUserStatus() != null && !UserStatus.isValid(request.getUserStatus())) {
            log.warn("Invalid user status '{}' provided in filter. Ignoring status filter.", request.getUserStatus());
            request.setUserStatus(null);
        }

        Page<UserResponse> result = userRepository.findByFilters(request, pageable)
                .map(userMapper::toResponse);
        log.info("Found {} users matching the criteria.", result.getTotalElements());
        return result;
    }

    @Override
    public UserResponse update(String id, UserCreateRequest request) {
        log.warn("Attempted to update user via update method, which is not supported. Use updateUser instead.");
        return null;
    }

    @Override
    @CachePut(value = "users", key = "#id")
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        log.info("Updating user with ID: {} with data: {}", id, request);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found for update.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage()));
                });
        UserGender userGender = user.getGender();

        try {
            userGender = UserGender.valueOf(request.getGender());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid gender '{}' provided for user ID {}. Keeping existing gender.", request.getGender(), id);
        }

        userMapper.updateUser(request, user);
        user.setGender(userGender);
        User saved = userRepository.save(user);
        log.info("User with ID {} updated successfully.", id);
        return userMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void updateStatus(String id, UserStatusSetRequest request) {
        log.info("Updating status for user ID: {} to isActive: {}.", id, request.getIsActive());
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found for status update.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage()));
                });

        User currentUser = userRepository
                .findByUsername(SecurityUtils.getCurrentUsername())
                .orElseThrow(() -> {
                    log.error("Current authenticated user not found in repository.");
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        if (currentUser.getUsername().equals(SecurityUtils.getCurrentUsername())) {
            user.setUserStatus(UserStatus.DISABLED_BY_USER);
            log.info("User ID {} self-disabled their account.", id);
        } else if (currentUser.getRole().getName().equals(UserType.SYS_ADMIN.name())
                || currentUser.getRole().getName().equals(UserType.ADMIN.name())) {
            user.setUserStatus(UserStatus.BANNED);
            log.info("Admin/SysAdmin {} banned user ID {}.", currentUser.getUsername(), id);
        } else {
            log.warn("Unauthorized attempt to change user status for user ID {} by user {}.", id, currentUser.getUsername());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        userRepository.save(user);
        log.info("User ID {} status updated to {}.", id, user.getUserStatus());
    }

    @Override
    public void deleteById(String id) {
        log.warn("Attempted to delete user by ID {}, which is not supported. Use updateStatus to disable/ban.", id);
        throw new UnsupportedOperationException("Direct deletion of users is not supported. Use updateStatus to disable or ban.");
    }

    @Override
    @CachePut(value = "users", key = "#id")
    public void updateUserRole(String id, UserRoleRequest request) {
        log.info("Updating role for user ID: {} to role: {}.", id, request.getRoleName());
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found for role update.", id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage()));
                });
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> {
                    log.warn("Role '{}' not found for role update.", request.getRoleName());
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage()));
                });
        user.setRole(role);
        userRepository.save(user);
        log.info("User ID {} role updated to {}.", id, role.getName());
    }
    @Override
    @CacheEvict(value = "users", key = "#id")
    public void updatePassword(String id, UpdatePasswordRequest request) {
        log.info("Attempting to update password for user ID: {}.", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found for password update.", id);
                    return new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())
                    );
                });

        User currentUser = userRepository
                .findByUsername(SecurityUtils.getCurrentUsername())
                .orElseThrow(() -> {
                    log.error("Current authenticated user not found in repository during password update.");
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        boolean isAdmin =
                currentUser.getRole().getName().equals(UserType.ADMIN.name()) ||
                currentUser.getRole().getName().equals(UserType.SYS_ADMIN.name());

        boolean isSelf = currentUser.getId().equals(id);

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            log.warn("Password update failed for user ID {}: New password and confirmation do not match.", id);
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        if (!isAdmin && isSelf) {
            if (!passwordEncoder.matches(
                    request.getOldPassword(),
                    user.getPassword()
            )) {
                log.warn("Password update failed for user ID {}: Invalid old password provided.", id);
                throw new BusinessException(ErrorCode.INVALID_OLD_PASSWORD);
            }
        }

        if (!isAdmin && !isSelf) {
            log.warn("Unauthorized attempt to change password for user ID {} by user {}.", id, currentUser.getUsername());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully updated for user ID {}.", id);
    }
}
