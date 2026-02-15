package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BudgetFilterRequest", description = "Request for filtering budgets")
public class BudgetFilterRequest {

    @Schema(description = "Period of the budget", example = "MONTHLY")
    String period;

    @Schema(description = "Name of the budget", example = "Food")
    String name;
}
