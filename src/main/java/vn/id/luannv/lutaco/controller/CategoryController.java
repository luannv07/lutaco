package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.service.CategoryService;
import vn.id.luannv.lutaco.util.LocalizationUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Category API",
        description = "API quản lý danh mục của người dùng, chỉ thao tác trên dữ liệu cá nhân"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class CategoryController {

    CategoryService categoryService;
    LocalizationUtils localizationUtils;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách danh mục",
            description = "Lấy danh sách danh mục của người dùng hiện tại, hỗ trợ lọc và phân trang"
    )
    public ResponseEntity<BaseResponse<List<CategoryDto>>> search(
            @ModelAttribute CategoryFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        categoryService.searchNoPag(request),
                        localizationUtils.getLocalizedMessage(MessageKeyConst.Success.SENT)
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Thêm danh mục mới",
            description = "Tạo mới một danh mục cho người dùng hiện tại"
    )
    public ResponseEntity<BaseResponse<Void>> create(
            @Valid @RequestBody CategoryDto request
    ) {
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(localizationUtils.getLocalizedMessage(MessageKeyConst.Success.CREATED)));
    }

    @PutMapping("/{categoryName}")
    @Operation(
            summary = "Cập nhật danh mục",
            description = "Cập nhật thông tin danh mục dựa trên tên danh mục"
    )
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable String categoryName,
            @Valid @RequestBody CategoryDto request
    ) {
        categoryService.update(categoryName, request);
        return ResponseEntity.ok(
                BaseResponse.success(localizationUtils.getLocalizedMessage(MessageKeyConst.Success.UPDATED))
        );
    }

    @PatchMapping("/{categoryName}/disabled")
    @Operation(
            summary = "Vô hiệu hoá danh mục",
            description = "Đánh dấu danh mục là không còn hoạt động (disable)"
    )
    public ResponseEntity<BaseResponse<Void>> updateStatus(
            @PathVariable String categoryName
    ) {
        categoryService.deleteById(categoryName);
        return ResponseEntity.ok(
                BaseResponse.success(localizationUtils.getLocalizedMessage(MessageKeyConst.Success.UPDATED))
        );
    }
}
