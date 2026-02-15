package vn.id.luannv.lutaco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "DTO quản lý danh mục dùng chung")
@Data
public class MasterDictionaryDto {

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.field.too_long}")
    @Schema(
            description = "Nhóm dữ liệu",
            example = "GENDER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String category;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.field.too_long}")
    @Schema(
            description = "Giá trị chuẩn",
            example = "MALE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String code;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.field.too_long}")
    @Schema(
            description = "Nhãn hiển thị",
            example = "Nam",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String valueVi;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.field.too_long}")
    @Schema(
            description = "Nhãn hiển thị",
            example = "Nam",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String valueEn;

    @NotNull(message = "{validation.required}")
    @Schema(
            description = "Trạng thái hoạt động",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Boolean isActive;
}

