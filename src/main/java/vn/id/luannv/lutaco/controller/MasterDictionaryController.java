package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
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
                        MessageKeyConst.Success.SENT
                ));
    }

    @GetMapping("/{category}/{code}")
    @Operation(
            summary = "Lấy dictionary theo category và code",
            description = "Dùng để map một giá trị cụ thể trong category (ví dụ: GENDER + MALE)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lấy dữ liệu thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
    public ResponseEntity<BaseResponse<MasterDictionaryDto>> getByCategoryAndCode(
            @Parameter(description = "Nhóm dữ liệu", example = "GENDER", required = true)
            @PathVariable String category,

            @Parameter(description = "Code chuẩn trong category", example = "MALE", required = true)
            @PathVariable String code
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        service.getByCategoryAndCode(category, code),
                        MessageKeyConst.Success.SENT
                ));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @Operation(
            summary = "Tạo mới dictionary",
            description = "Tạo mới một giá trị dictionary dùng cho cấu hình hệ thống"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tạo mới thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
    public ResponseEntity<BaseResponse<MasterDictionaryDto>> create(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin dictionary cần tạo",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
            )
            @RequestBody MasterDictionaryDto dto
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        service.create(dto),
                        MessageKeyConst.Success.CREATED
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    @Operation(
            summary = "Cập nhật dictionary",
            description = "Cập nhật thông tin dictionary dựa trên id"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
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
                        MessageKeyConst.Success.UPDATED
                ));
    }
}
