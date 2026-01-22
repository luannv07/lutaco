package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.RoleFilterRequest;
import vn.id.luannv.lutaco.dto.request.RoleUpdateRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.service.RoleService;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Role API", description = "Các API quản lý vai trò (Role)")
public class RoleController {

    RoleService roleService;

    @GetMapping
    @Operation(summary = "Tìm kiếm danh sách role")
    @PreAuthorize("hasAuthority('SYS_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<Page<Role>>> getAllRoles(@ModelAttribute RoleFilterRequest request) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        roleService.search(request.getName(), request.getPage(), request.getSize()),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết role theo id")
    @PreAuthorize("hasAuthority('SYS_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<Role>> getRoleById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        roleService.getDetail(id),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật role")
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<Role>> updateRole(
            @PathVariable Integer id,
            @RequestBody RoleUpdateRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        roleService.update(id, request),
                        MessageKeyConst.Success.UPDATED
                )
        );
    }
}
