package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.CategoryService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.JwtUtils;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Category API", description = "API quản lý danh mục")
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
/**
 * Toàn bộ các api bên dưới đều thao tác với data của chính bản thân
 */
public class CategoryController {

    CategoryService categoryService;

    @Operation(summary = "Lấy danh sách danh mục của chính mình")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<CategoryDto>>> search(
            @ModelAttribute CategoryFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        categoryService.search(request, request.getPage(), request.getSize()),
                        MessageKeyConst.Success.SENT
                )
        );
    }

    @Operation(summary = "Thêm danh mục")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(
            @Valid @RequestBody CategoryDto request
    ) {
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(null, MessageKeyConst.Success.CREATED));
    }

    @Operation(summary = "Sửa danh mục")
    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable String id,
            @Valid @RequestBody CategoryDto request
    ) {
        categoryService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success(null, MessageKeyConst.Success.UPDATED)
        );
    }

    @Operation(summary = "Disable danh mục")
    @PatchMapping("/{id}/disabled")
    public ResponseEntity<BaseResponse<Void>> updateStatus(@PathVariable String id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success(null, MessageKeyConst.Success.UPDATED)
        );
    }
}

