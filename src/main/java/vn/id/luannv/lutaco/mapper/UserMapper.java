package vn.id.luannv.lutaco.mapper;

import org.mapstruct.*;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.request.UserUpdateRequest;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userStatus", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "userPlan", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    User toEntity(UserCreateRequest request);

    @Mapping(target = "roleName", source = "role.name")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userStatus", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "userPlan", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);
}
