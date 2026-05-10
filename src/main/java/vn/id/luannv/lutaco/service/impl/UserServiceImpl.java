package vn.id.luannv.lutaco.service.impl;

import jakarta.persistence.criteria.Join;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;
import vn.id.luannv.lutaco.service.RefreshTokenService;
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenService invalidatedTokenService;
    RefreshTokenService refreshTokenService;

    private Specification<User> buildSpec(UserFilterRequest req) {
        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            // username (LIKE)
            if (req.getUsername() != null && !req.getUsername().isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("username")),
                                "%" + req.getUsername().toLowerCase() + "%"));
            }

            // userStatus (ENUM)
            if (req.getUserStatus() != null && !req.getUserStatus().isBlank()) {
                UserStatus status = UserStatus.valueOf(req.getUserStatus());
                predicates = cb.and(predicates,
                        cb.equal(root.get("userStatus"), status));
            }

            // userPlan (ENUM)
            if (req.getUserPlan() != null && !req.getUserPlan().isBlank()) {
                UserPlan plan = UserPlan.valueOf(req.getUserPlan());
                predicates = cb.and(predicates,
                        cb.equal(root.get("userPlan"), plan));
            }

            // roleId (JOIN)
            if (req.getRoleId() != null) {
                Join<User, Role> roleJoin = root.join("role");
                predicates = cb.and(predicates,
                        cb.equal(roleJoin.get("id"), req.getRoleId()));
            }

            return predicates;
        };
    }

    @Override
    public UserResponse create(UserCreateRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public UserResponse getDetail(Long id) {
        return userRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));
    }

    @Override
    public Page<UserResponse> search(UserFilterRequest request) {
        Specification<User> spec = buildSpec(request);
        Page<User> users = userRepository.findAll(spec, request.pageable());
        return users.map(this::convertToResponse);
    }

    @Override
    public UserResponse update(Long id, UserCreateRequest request) {
        // this function is not supported
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public UserResponse updateUserBaseInfo(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));

        UserGender userGender = EnumUtils.from(UserGender.class, request.getGender());
        user.setFullName(request.getFullName());
        user.setGender(userGender);
        userRepository.save(user);
        return convertToResponse(user);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UserStatusSetRequest request) {
        User actor = userRepository.findById(SecurityUtils.getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));

        User target = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));

        UserType actorRole = actor.getRole().getCode();
        UserType targetRole = target.getRole().getCode();

        // SYS_ADMIN không thể tự disable/ban chính mình
        if (actorRole == UserType.SYS_ADMIN && actor.getId().equals(id)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // ADMIN không thể thay đổi trạng thái của SYS_ADMIN
        if (actorRole == UserType.ADMIN && targetRole == UserType.SYS_ADMIN) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // USER chỉ có thể cập nhật trạng thái của chính mình
        if (actorRole == UserType.USER && !actor.getId().equals(id)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // Nếu là USER và đang PENDING, cho phép cập nhật trạng thái từ request
        // Còn nếu là ADMIN hoặc SYS_ADMIN, luôn cho phép update
        if (actorRole == UserType.USER && actor.getUserStatus() != UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        UserStatus newStatus = EnumUtils.from(UserStatus.class, request.getStatus());
        target.setUserStatus(newStatus);

        userRepository.save(target);
        log.info("User status updated: id={}, newStatus={}, actor={}", id, newStatus, actor.getId());
    }

    @Override
    public void deleteById(Long id) {
        log.warn("[system]: Attempted to delete user by ID {}, which is not supported. Use updateStatus to disable/ban.", id);
        throw new UnsupportedOperationException("Direct deletion of users is not supported. Use updateStatus to disable or ban.");
    }

    @Override
    @Transactional
    public void updateUserRole(Long id, UserRoleRequest request) {
        UserType userType = EnumUtils.from(UserType.class, request.getRoleName());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));
        Role role = roleRepository.findByCode(userType)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("roleName", request.getRoleName())));
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, UpdatePasswordRequest request, String jti, Instant expiryTime) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("id", id)));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        if (request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        invalidatedTokenService.addInvalidatedToken(jti, expiryTime);
        refreshTokenService.deleteAllByUsername(user.getUsername());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        log.info("User is logging out...");
        userRepository.save(user);
    }

    @Override
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("email", email)));
        return convertToResponse(user);
    }

    private UserResponse convertToResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .gender(user.getGender())
                .userStatus(user.getUserStatus())
                .roleName(user.getRole().getCode())
                .createdBy(user.getCreatedBy())
                .createdDate(user.getCreatedDate())
                .updatedBy(user.getUpdatedBy())
                .updatedDate(user.getUpdatedDate())
                .userPlan(user.getUserPlan())
                .build();
    }
}
