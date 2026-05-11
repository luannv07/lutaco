package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    String name;

    @NotNull(message = "{validation.required}")
    Long categoryId;

    @NotNull(message = "{validation.required}")
    @Positive(message = "{validation.field.positive}")
    BigDecimal targetAmount;

    @NotBlank(message = "{validation.required}")
    String period;

    @NotNull(message = "{validation.required}")
    LocalDate startDate;
}
