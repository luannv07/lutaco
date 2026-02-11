package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import vn.id.luannv.lutaco.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "User API",
        description = "API quản lý người dùng hệ thống (tìm kiếm, cập nhật thông tin, phân quyền, trạng thái)"
)
@PreAuthorize("isAuthenticated()")
public class UserController {

    UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "Lấy danh sách người dùng",
            description = "Tìm kiếm và phân trang danh sách người dùng theo các tiêu chí lọc"
    )
    public ResponseEntity<BaseResponse<Page<UserResponse>>> getUsers(
            @Parameter(description = "Điều kiện lọc và phân trang người dùng")
            @Valid  @ModelAttribute UserFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        userService.search(request, request.getPage(), request.getSize()),
                        "Lấy danh sách người dùng thành công."
                ));
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasRole('SYS_ADMIN') or hasRole('ADMIN') or #id == authentication.principal.id and @securityPermission.isActive()"
    )
    @Operation(
            summary = "Lấy chi tiết người dùng",
            description = "Lấy thông tin chi tiết người dùng theo ID"
    )
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @Parameter(description = "ID người dùng", example = "USR_123456", required = true)
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
    @Operation(
            summary = "Cập nhật thông tin người dùng",
            description = "Cập nhật thông tin hồ sơ người dùng theo ID"
    )
    public ResponseEntity<BaseResponse<UserResponse>> update(
            @Parameter(description = "ID người dùng", example = "USR_123456", required = true)
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
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @Operation(
            summary = "Cập nhật quyền người dùng",
            description = "Thay đổi role của người dùng (chỉ SYS_ADMIN)"
    )
    public ResponseEntity<BaseResponse<Void>> update(
            @Parameter(description = "ID người dùng", example = "USR_123456", required = true)
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
            "hasRole('SYS_ADMIN') or hasRole('ADMIN') or (#id == authentication.principal.id and #request.isActive == false)"
    )
    @Operation(
            summary = "Cập nhật trạng thái người dùng",
            description = "Kích hoạt hoặc vô hiệu hoá tài khoản người dùng theo ID"
    )
    public ResponseEntity<BaseResponse<Void>> updateUserStatus(
            @Parameter(description = "ID người dùng", example = "USR_123456", required = true)
            @PathVariable String id,
            @Valid  @RequestBody UserStatusSetRequest request
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
    @Operation(
            summary = "Cập nhật mật khẩu",
            description = "Người dùng tự đổi mật khẩu hoặc admin đổi mật khẩu cho người dùng"
    )
    public ResponseEntity<BaseResponse<Void>> updatePassword(
            @Parameter(description = "ID người dùng", example = "USR_123456", required = true)
            @PathVariable String id,
            @Valid  @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(id, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật mật khẩu thành công.")
        );
    }
}
