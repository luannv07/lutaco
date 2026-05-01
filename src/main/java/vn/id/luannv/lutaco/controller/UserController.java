package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.service.UserService;
import vn.id.luannv.lutaco.util.JwtUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    JwtService jwtService;

    @GetMapping
    @PreAuthorize("(hasRole('SYS_ADMIN') or hasRole('ADMIN')) and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Page<UserResponse>>> getUsers(
            @Valid @ModelAttribute UserFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        userService.search(request, request.getPage(), request.getSize()),
                        "Lấy danh sách người dùng thành công."
                ));
    }

    @GetMapping("/me")
    @PreAuthorize("@securityPermission.isLoggedIn()")
    public ResponseEntity<BaseResponse<UserResponse>> getMe() {
        return ResponseEntity.ok(
                BaseResponse.success(
                        userService.getDetail(SecurityUtils.getCurrentId()),
                        "Lấy chi tiết người dùng thành công."
                ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("(hasRole('SYS_ADMIN') or hasRole('ADMIN')) and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        userService.getDetail(id),
                        "Lấy chi tiết người dùng thành công."
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "(hasRole('SYS_ADMIN') or hasRole('ADMIN') or #id == authentication.principal.id) and @securityPermission.isActive()"
    )
    public ResponseEntity<BaseResponse<UserResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        userService.updateUser(id, request),
                        "Cập nhật thông tin người dùng thành công."
                ));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SYS_ADMIN') and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable String id,
            @Valid @RequestBody UserRoleRequest request
    ) {
        userService.updateUserRole(id, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật quyền người dùng thành công.")
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasRole('SYS_ADMIN') or hasRole('ADMIN') or (#id == authentication.principal.id and @securityPermission.isActive())"
    )
    public ResponseEntity<BaseResponse<Void>> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UserStatusSetRequest request
    ) {
        userService.updateStatus(id, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật trạng thái người dùng thành công.")
        );
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize(
            "(hasRole('SYS_ADMIN') or hasRole('ADMIN') or #id == authentication.principal.id) and @securityPermission.isActive()"
    )
    public ResponseEntity<BaseResponse<Void>> updatePassword(
            @PathVariable String id,
            @Valid @RequestBody UpdatePasswordRequest request, HttpServletRequest req
    ) {
        String token = JwtUtils.resolveToken(req);
        String jti = jwtService.getJtiFromToken(token);
        Date expiryTime = jwtService.getExpiryTimeFromToken(token);
        userService.updatePassword(id, request, jti, expiryTime);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật mật khẩu thành công.")
        );
    }
}
