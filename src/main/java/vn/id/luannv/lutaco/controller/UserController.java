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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.UserResponse;
import vn.id.luannv.lutaco.service.UserService;


@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User API", description = "API quản lý người dùng")
public class UserController {

    UserService userService;

    @Operation(summary = "Lấy danh sách người dùng", description = "Tìm kiếm và phân trang danh sách người dùng")
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<UserResponse>>> getUsers(
            @Parameter(description = "Các tiêu chí lọc người dùng") @ModelAttribute UserFilterRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        userService.search(request, request.getPage(), request.getSize()),
                        MessageKeyConst.Success.SENT
                ));
    }

    @Operation(summary = "Lấy thông tin chi tiết người dùng", description = "Lấy chi tiết người dùng theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('ADMIN') or #id == authentication.principal.id and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<UserResponse>> getUser(
            @Parameter(description = "ID người dùng cần lấy") @PathVariable String id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        userService.getDetail(id),
                        MessageKeyConst.Success.SENT
                ));
    }

    @Operation(summary = "Cập nhật người dùng", description = "Cập nhật thông tin người dùng theo ID")
    @PutMapping("/{id}")
    @PreAuthorize("(hasRole('SYS_ADMIN') or hasRole('ADMIN') or #id == authentication.principal.id) and @securityPermission.isActive()")
    public ResponseEntity<BaseResponse<UserResponse>> update(
            @Parameter(description = "ID người dùng cần cập nhật") @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        userService.updateUser(id, request),
                        MessageKeyConst.Success.UPDATED
                ));
    }

    @Operation(summary = "Cập nhật quyền người dùng", description = "Cập nhật thông tin người dùng theo ID")
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> update(
            @Parameter(description = "ID người dùng cần cập nhật") @PathVariable String id,
            @Valid @RequestBody UserRoleRequest request
    ) {
        userService.updateUserRole(id, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        null,
                        MessageKeyConst.Success.UPDATED
                ));
    }

    @Operation(summary = "Chỉnh sửa trạng thái người dùng", description = "Chỉnh sửa trạng thái người dùng theo ID")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('ADMIN') or (#id == authentication.principal.id and #request.isActive == false)")
    public ResponseEntity<BaseResponse<Void>> updateUserStatus(
            @Parameter(description = "ID người dùng cần cập nhật") @PathVariable String id, @RequestBody UserStatusSetRequest request) {
        userService.updateStatus(id, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        null,
                        MessageKeyConst.Success.DELETED
                ));
    }

    @Operation(
            summary = "Cập nhật mật khẩu",
            description = "Người dùng tự đổi mật khẩu hoặc admin đổi mật khẩu cho user"
    )
    @PatchMapping("/{id}/password")
    @PreAuthorize(
            "(hasRole('SYS_ADMIN') or hasRole('ADMIN') or #id == authentication.principal.id) and @securityPermission.isActive()"
    )
    public ResponseEntity<BaseResponse<Void>> updatePassword(
            @Parameter(description = "ID người dùng cần cập nhật")
            @PathVariable String id,
            @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(id, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        null,
                        MessageKeyConst.Success.UPDATED
                ));
    }

}
