package vn.id.luannv.lutaco.service;

import jakarta.validation.Valid;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;

import java.time.Instant;

public interface UserService extends
        BaseService<UserFilterRequest, UserResponse, UserCreateRequest, Long> {
    void updateUserRole(Long id, @Valid UserRoleRequest request);

    UserResponse updateUserBaseInfo(Long id, @Valid UserUpdateRequest request);

    void updatePassword(Long id, UpdatePasswordRequest request, String jti, Instant expiryTime);

    void updateStatus(Long id, UserStatusSetRequest request);

    UserResponse getByEmail(String email);
}
