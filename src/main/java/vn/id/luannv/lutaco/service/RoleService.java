package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.enumerate.UserType;

public interface RoleService
        extends BaseService<String, Role, Role, Integer> {
    Role getByRoleCode(UserType userType);
}
