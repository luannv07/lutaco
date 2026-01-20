package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.request.RoleUpdateRequest;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.service.RoleService;

import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;

    @Override
    public Role create(RoleUpdateRequest request) {
        log.info("RoleServiceImpl create: {}", request);

        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    @Override
    @Cacheable(value = "role_detail", key = "#id", condition = "#id > 0")
    public Role getDetail(Integer id) {
        log.info("RoleServiceImpl getDetail: {}", id);

        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())
                        ));
    }

    @Override
    @Cacheable(value = "role_list")
    public Page<Role> search(String name, Integer page, Integer size) {
        log.info("RoleServiceImpl search: name={}", name);

        Pageable pageable = PageRequest.of(page-1, size);

        if (name == null || name.isBlank()) {
            return roleRepository.findAll(pageable);
        }

        return roleRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @CachePut(value = "role_detail", key = "#id", condition = "#id > 0")
    public Role update(Integer id, RoleUpdateRequest request) {
        log.info("RoleServiceImpl update: id={}, request={}", id, request);

        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())
                        ));

        role.setDescription(request.getDescription());

        return role;
    }

    @Override
    @CachePut(value = "role_detail", key = "#id", condition = "#id > 0")
    public void deleteById(Integer id) {
        log.info("RoleServiceImpl deleteById: {}", id);

        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
}
