package vn.id.luannv.lutaco.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetDetailRequest {

    @NotBlank(message = "{input.required}")
    String budgetName;
}
