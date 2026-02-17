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
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.service.RoleService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;

    @Override
    public Role create(Role request) {
        log.warn("[{}]: Attempted to create a role via create method, which is not supported. Request: {}", SecurityUtils.getCurrentUsername(), request);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    @Override
    @Cacheable(value = "roles", key = "#id")
    public Role getDetail(Integer id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Fetching details for role with ID: {}", username, id);
        return roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Role with ID {} not found.", username, id);
                    return new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND,
                            Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())
                    );
                });
    }

    @Override
    public Page<Role> search(String name, Integer page, Integer size) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Searching roles with name: '{}', page: {}, size: {}.", username, name, page, size);
        Pageable pageable = PageRequest.of(page - 1, size);

        if (name == null || name.isBlank()) {
            Page<Role> roles = roleRepository.findAll(pageable);
            log.info("[{}]: Found {} roles without name filter.", username, roles.getTotalElements());
            return roles;
        }

        Page<Role> roles = roleRepository.findByNameContainingIgnoreCase(name, pageable);
        log.info("[{}]: Found {} roles matching name '{}'.", username, roles.getTotalElements(), name);
        return roles;
    }

    @Override
    @CacheEvict(value = "roles", key = "#id")
    public Role update(Integer id, Role request) {
        log.warn("[{}]: Attempted to update a role via update method, which is not supported. ID: {}, Request: {}", SecurityUtils.getCurrentUsername(), id, request);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR); // Or a more specific error if role updates are not allowed
    }

    @Override
    @CacheEvict(value = "roles", key = "#id")
    public void deleteById(Integer id) {
        log.warn("[{}]: Attempted to delete a role via deleteById method, which is not supported. ID: {}", SecurityUtils.getCurrentUsername(), id);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
}
