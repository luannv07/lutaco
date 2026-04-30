package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.service.RoleService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;

    @Override
    public Role create(Role request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Role getDetail(Integer id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Page<Role> search(String name, Integer page, Integer size) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Role update(Integer id, Role request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void deleteById(Integer id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public Role getByRoleCode(UserType userType) {
        return roleRepository.findByCode(userType);
    }
}
