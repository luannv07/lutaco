package vn.id.luannv.lutaco.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.id.luannv.lutaco.dto.request.CreateBudgetRequest;
import vn.id.luannv.lutaco.dto.request.UpdateBudgetRequest;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.entity.Budget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "user.username", target = "username")
    BudgetResponse toBudgetResponse(Budget budget);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "actualAmount", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Budget toBudget(CreateBudgetRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "actualAmount", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void updateBudgetFromRequest(UpdateBudgetRequest request, @MappingTarget Budget budget);
}
