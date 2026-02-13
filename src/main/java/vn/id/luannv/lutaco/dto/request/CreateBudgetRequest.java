package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBudgetRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.field.too_long}")
    String name;

    @NotBlank(message = "{validation.required}")
    String categoryId;

    @NotNull(message = "{validation.required}")
    @Range(min = 1, max = 1000000000, message = "{validation.field.size_not_in_range}")

    Long targetAmount;

    @NotBlank(message = "{validation.required}")
    String period;

    LocalDateTime startDate;
}
