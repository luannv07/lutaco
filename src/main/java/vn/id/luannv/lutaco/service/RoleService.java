package vn.id.luannv.lutaco.service;

import org.springframework.data.domain.Page;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.enumerate.UserType;

public interface RoleService
        extends BaseService<String, Role, Role, Integer> {
    Page<Role> literalSearch(String name, Integer page, Integer size);

    Role getByRoleCode(UserType userType);
}
