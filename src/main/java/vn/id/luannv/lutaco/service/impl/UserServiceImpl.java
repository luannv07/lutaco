package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.MasterDictionaryDto;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.MasterDictionaryType;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.UserMapper;
import vn.id.luannv.lutaco.repository.MasterDictionaryRepository;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.MasterDictionaryService;
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    MasterDictionaryService masterDictionaryService;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(UserCreateRequest request) {return null;}

    @Override
    @Cacheable(value = "user_detail", key = "#id")
    public UserResponse getDetail(String id) {
        log.info("UserServiceImpl getDetail: {}", id);

        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())));
    }

    @Override
    public Page<UserResponse> search(UserFilterRequest request, Integer page, Integer size) {
        log.info("UserServiceImpl search: {}", request);
        Pageable pageable = PageRequest.of(page-1, size);

        if (request.getUserStatus() != null && !UserStatus.isValid(request.getUserStatus()))
            request.setUserStatus(null);

        return userRepository.findByFilters(request, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public UserResponse update(String id, UserCreateRequest request) {
        return null;
    }

    @Override
    @CachePut(value = "user_detail", key = "#id")
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        log.info("UserServiceImpl update User: {} {}", id, request);
        MasterDictionaryDto dictionaryDto = masterDictionaryService.getByCategoryAndCode(MasterDictionaryType.GENDER.name(), request.getGender());

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())));

        userMapper.updateUser(request, user);
        user.setGender(dictionaryDto.getCode());
        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }

    @Override
    @CachePut(value = "user_detail", key = "#id")
    public void updateStatus(String id, UserStatusSetRequest request) {
        log.info("UserServiceImpl updateStatus: {} {}", id, request);
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())));
        log.info("{} {}", user.getRole().getName(), UserType.SYS_ADMIN.name());

        User currentUser = userRepository
                .findByUsername(SecurityUtils.getCurrentUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (currentUser.getUsername().equals(SecurityUtils.getCurrentUsername()))
            user.setUserStatus(UserStatus.DISABLED_BY_USER);
        else if (currentUser.getRole().getName().equals(UserType.SYS_ADMIN.name())
                || currentUser.getRole().getName().equals(UserType.ADMIN.name()))
            user.setUserStatus(UserStatus.BANNED);

        userRepository.save(user);
    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    @CachePut(value = "user_detail", key = "#id")
    public void updateUserRole(String id, UserRoleRequest request) {
        log.info("UserServiceImpl updateUserRole: {}", request);
        // need to update
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())));
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())));
        user.setRole(role);
        userRepository.save(user);
    }
    @Override
    @Cacheable(value = "user_detail", key = "#id")
    public void updatePassword(String id, UpdatePasswordRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.ENTITY_NOT_FOUND,
                                Map.of("id", ErrorCode.ENTITY_NOT_FOUND.getMessage())
                        )
                );

        User currentUser = userRepository
                .findByUsername(SecurityUtils.getCurrentUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        boolean isAdmin =
                currentUser.getRole().getName().equals(UserType.ADMIN.name()) ||
                currentUser.getRole().getName().equals(UserType.SYS_ADMIN.name());

        boolean isSelf = currentUser.getId().equals(id);

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        if (!isAdmin && isSelf) {
            if (!passwordEncoder.matches(
                    request.getOldPassword(),
                    user.getPassword()
            )) {
                throw new BusinessException(ErrorCode.INVALID_OLD_PASSWORD);
            }
        }

        if (!isAdmin && !isSelf) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
