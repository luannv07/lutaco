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

    @NotBlank(message = "{input.required}")
    @Size(max = 50, message = "{input.tooLong}")
    @Schema(
            description = "Nhóm dữ liệu",
            example = "GENDER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String category;

    @NotBlank(message = "{input.required}")
    @Size(max = 50, message = "{input.tooLong}")
    @Schema(
            description = "Giá trị chuẩn",
            example = "MALE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String code;

    @NotBlank(message = "{input.required}")
    @Size(max = 100, message = "{input.tooLong}")
    @Schema(
            description = "Nhãn hiển thị",
            example = "Nam",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String valueVi;

    @NotBlank(message = "{input.required}")
    @Size(max = 100, message = "{input.tooLong}")
    @Schema(
            description = "Nhãn hiển thị",
            example = "Nam",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String valueEn;

    @NotNull(message = "{input.required}")
    @Schema(
            description = "Trạng thái hoạt động",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Boolean isActive;
}

