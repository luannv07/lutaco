package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.RoleFilterRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.service.RoleService;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(
        name = "Role API",
        description = "API quản lý vai trò hệ thống (Role), chỉ dành cho admin"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class RoleController {

    RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "Tìm kiếm danh sách role",
            description = "Lấy danh sách role trong hệ thống, hỗ trợ tìm kiếm theo tên và phân trang"
    )
    public ResponseEntity<BaseResponse<Page<Role>>> getAllRoles(
            @Parameter(description = "Điều kiện lọc và phân trang")
            @ModelAttribute RoleFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        roleService.search(
                                request.getName(),
                                request.getPage(),
                                request.getSize()
                        ),
                        "Lấy danh sách vai trò thành công."
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "Lấy chi tiết role",
            description = "Lấy thông tin chi tiết của một role theo id"
    )
    public ResponseEntity<BaseResponse<Role>> getRoleById(
            @Parameter(
                    description = "ID của role",
                    example = "1",
                    required = true
            )
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        roleService.getDetail(id),
                        "Lấy chi tiết vai trò thành công."
                )
        );
    }
}
