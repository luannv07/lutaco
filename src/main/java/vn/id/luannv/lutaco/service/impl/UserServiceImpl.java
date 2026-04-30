package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.util.Date;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenService invalidatedTokenService;
    LocalizationUtils localizationUtils;

    @Override
    public UserResponse create(UserCreateRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public UserResponse getDetail(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Page<UserResponse> search(UserFilterRequest request, Integer page, Integer size) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public UserResponse update(String id, UserCreateRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void updateStatus(String id, UserStatusSetRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void deleteById(String id) {
        log.warn("[system]: Attempted to delete user by ID {}, which is not supported. Use updateStatus to disable/ban.", id);
        throw new UnsupportedOperationException("Direct deletion of users is not supported. Use updateStatus to disable or ban.");
    }

    @Override
    @Transactional
    public void updateUserRole(String id, UserRoleRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void updatePassword(String id, UpdatePasswordRequest request, String jti, Date expiryTime) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    private UserResponse convertToResponse(User user) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
