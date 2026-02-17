package vn.id.luannv.lutaco.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "BudgetFilterRequest", description = "Request for filtering budgets")
public class BudgetFilterRequest extends BaseFilterRequest{

    @Schema(description = "Period of the budget", example = "MONTHLY")
    String period;

    @Schema(description = "Name of the budget", example = "Food")
    String name;
}
