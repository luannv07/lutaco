package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
        description = "API quản lý danh mục dùng chung (lookup / cấu hình động)"
)
public class MasterDictionaryController {

    MasterDictionaryService service;

    @Operation(
            summary = "Lấy danh sách theo category",
            description = "Trả về danh sách giá trị đang active theo category (ví dụ: GENDER, USER_STATUS)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
    @GetMapping("/{category}")
    public ResponseEntity<BaseResponse<List<MasterDictionaryDto>>> getByCategory(
            @Parameter(
                    description = "Nhóm dữ liệu (GENDER, USER_STATUS, ...)",
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

    @Operation(
            summary = "Lấy chi tiết theo category và code",
            description = "Dùng khi cần map giá trị cụ thể (ví dụ: GENDER + MALE)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
    @GetMapping("/{category}/{code}")
    public ResponseEntity<BaseResponse<MasterDictionaryDto>> getByCategoryAndCode(
            @Parameter(description = "Nhóm dữ liệu", example = "GENDER", required = true)
            @PathVariable String category,

            @Parameter(description = "Giá trị chuẩn", example = "MALE", required = true)
            @PathVariable String code
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(
                        service.getByCategoryAndCode(category, code),
                        MessageKeyConst.Success.SENT
                ));
    }

    @Operation(
            summary = "Tạo mới dictionary",
            description = "Tạo mới một giá trị cấu hình hệ thống"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tạo thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
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

    @Operation(
            summary = "Cập nhật dictionary",
            description = "Cập nhật thông tin dictionary theo id"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = MasterDictionaryDto.class))
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<BaseResponse<MasterDictionaryDto>> update(
            @Parameter(description = "ID dictionary", example = "1", required = true)
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
