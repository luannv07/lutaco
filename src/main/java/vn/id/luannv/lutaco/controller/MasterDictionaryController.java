package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import vn.id.luannv.lutaco.dto.MasterDictionaryDto;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.service.MasterDictionaryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-dictionary")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Master Dictionary",
        description = "API quản lý dữ liệu dùng chung (lookup, cấu hình động cho hệ thống)"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class MasterDictionaryController {

    MasterDictionaryService service;

    @GetMapping("/{category}")
    @Operation(
            summary = "Lấy danh sách dictionary theo category",
            description = "Trả về danh sách các giá trị đang active theo category (ví dụ: GENDER, USER_STATUS)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy category")
    })
    public ResponseEntity<BaseResponse<List<MasterDictionaryDto>>> getByCategory(
            @Parameter(
                    description = "Nhóm dữ liệu dictionary",
                    example = "GENDER",
                    required = true
            )
            @PathVariable String category
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        service.getByCategory(category),
                        "Lấy danh sách thành công."
                ));
    }

    @GetMapping("/{category}/{code}")
    @Operation(
            summary = "Lấy dictionary theo category và code",
            description = "Dùng để map một giá trị cụ thể trong category (ví dụ: GENDER + MALE)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy dữ liệu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy dữ liệu")
    })
    public ResponseEntity<BaseResponse<MasterDictionaryDto>> getByCategoryAndCode(
            @Parameter(description = "Nhóm dữ liệu", example = "GENDER", required = true)
            @PathVariable String category,

            @Parameter(description = "Code chuẩn trong category", example = "MALE", required = true)
            @PathVariable String code
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        service.getByCategoryAndCode(category, code),
                        "Lấy dữ liệu thành công."
                ));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @Operation(
            summary = "Tạo mới dictionary",
            description = "Tạo mới một giá trị dictionary dùng cho cấu hình hệ thống"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo mới thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<BaseResponse<MasterDictionaryDto>> create(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin dictionary cần tạo",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
            )
            @RequestBody MasterDictionaryDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        service.create(dto),
                        "Tạo mới thành công."
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @Operation(
            summary = "Cập nhật dictionary",
            description = "Cập nhật thông tin dictionary dựa trên id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy dictionary")
    })
    public ResponseEntity<BaseResponse<MasterDictionaryDto>> update(
            @Parameter(description = "ID của dictionary", example = "1", required = true)
            @PathVariable Integer id,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin dictionary cần cập nhật",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
            )
            @RequestBody MasterDictionaryDto dto
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        service.update(id, dto),
                        "Cập nhật thành công."
                ));
    }
}
