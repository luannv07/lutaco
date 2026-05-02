package vn.id.luannv.lutaco.service;

import jakarta.validation.Valid;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.User;

import java.util.Date;

public interface UserService extends
        BaseService<UserFilterRequest, UserResponse, UserCreateRequest, String> {
    void updateUserRole(String id, @Valid UserRoleRequest request);

    UserResponse updateUser(String id, @Valid UserUpdateRequest request);

    void updatePassword(String id, UpdatePasswordRequest request, String jti, Date expiryTime);

    void updateStatus(Long id, UserStatusSetRequest request);

    UserResponse getByEmail(String email);
}
