package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterDictionaryRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.field.too_long}")
    String dictGroup;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.field.too_long}")
    String code;

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    String valueVi;

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    String valueEn;

    @NotNull(message = "{validation.required}")
    Boolean activeFlg;

    Integer displayOrder;
}
