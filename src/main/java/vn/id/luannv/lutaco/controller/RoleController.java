package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
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
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class RoleController {

    RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Page<Role>>> getAllRoles(
            @Valid @ModelAttribute RoleFilterRequest request
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
    public ResponseEntity<BaseResponse<Role>> getRoleById(
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
