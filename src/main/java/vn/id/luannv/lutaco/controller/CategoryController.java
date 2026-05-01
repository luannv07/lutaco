package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.dto.request.CategoryRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.CategoryResponse;
import vn.id.luannv.lutaco.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class CategoryController {

    CategoryService categoryService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<CategoryResponse>>> search(
            @Valid @ModelAttribute CategoryFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        categoryService.search(request, request.getPage(), request.getSize()),
                        "Lấy danh sách danh mục thành công."
                )
        );
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> search(@PathVariable String id) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        categoryService.getChildren(id),
                        "Lấy danh sách danh mục thành công."
                )
        );
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(
            @Valid @RequestBody CategoryRequest request
    ) {
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Tạo danh mục thành công."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request
    ) {
        categoryService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật danh mục thành công.")
        );
    }

    @PatchMapping("/{id}/disabled")
    public ResponseEntity<BaseResponse<Void>> updateStatus(
            @PathVariable String id
    ) {
        categoryService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success("Vô hiệu hoá danh mục thành công.")
        );
    }
}
