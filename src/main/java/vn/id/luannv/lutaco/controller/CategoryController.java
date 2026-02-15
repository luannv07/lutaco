package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Category",
        description = "API quản lý danh mục của người dùng, chỉ thao tác trên dữ liệu cá nhân"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class CategoryController {

    CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách danh mục",
            description = "Lấy danh sách danh mục của người dùng hiện tại, hỗ trợ lọc và phân trang"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách danh mục thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<BaseResponse<List<CategoryDto>>> search(
            @Valid  @ModelAttribute CategoryFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        categoryService.searchNoPag(request),
                        "Lấy danh sách danh mục thành công."
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Thêm danh mục mới",
            description = "Tạo mới một danh mục cho người dùng hiện tại"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo danh mục thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "409", description = "Danh mục đã tồn tại")
    })
    public ResponseEntity<BaseResponse<Void>> create(
            @Valid @RequestBody CategoryDto request
    ) {
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Tạo danh mục thành công."));
    }

    @PutMapping("/{categoryName}")
    @Operation(
            summary = "Cập nhật danh mục",
            description = "Cập nhật thông tin danh mục dựa trên tên danh mục"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật danh mục thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<BaseResponse<Void>> update(
            @PathVariable String categoryName,
            @Valid @RequestBody CategoryDto request
    ) {
        categoryService.update(categoryName, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật danh mục thành công.")
        );
    }

    @PatchMapping("/{categoryName}/disabled")
    @Operation(
            summary = "Vô hiệu hoá danh mục",
            description = "Đánh dấu danh mục là không còn hoạt động (disable)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vô hiệu hoá danh mục thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<BaseResponse<Void>> updateStatus(
            @PathVariable String categoryName
    ) {
        categoryService.deleteById(categoryName);
        return ResponseEntity.ok(
                BaseResponse.success("Vô hiệu hoá danh mục thành công.")
        );
    }
}
