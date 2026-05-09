package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetUpdateRequest {

    @Size(max = 255, message = "{validation.field.too_long}")
    String name;

    @Positive(message = "{validation.field.positive}")
    BigDecimal targetAmount;

    LocalDate startDate;

    LocalDate endDate;
}
