package vn.id.luannv.lutaco.service;

import jakarta.validation.Valid;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;

public interface UserService extends
        BaseService<UserFilterRequest, UserResponse, UserCreateRequest, String>{
    void updateUserRole(String id, @Valid UserRoleRequest request);

    UserResponse updateUser(String id, @Valid UserUpdateRequest request);

    void updatePassword(String id, UpdatePasswordRequest request);

    void updateStatus(String id, UserStatusSetRequest request);
}
