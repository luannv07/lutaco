package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Cập nhật budget")
public class BudgetUpdateRequest {

    @NotBlank(message = "{input.required}")
    @Length(max = 100, message = "{input.tooLong}")
    String budgetName;

    @Length(max = 255, message = "{input.tooLong}")
    String description;
}
