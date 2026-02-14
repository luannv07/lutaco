package vn.id.luannv.lutaco.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.id.luannv.lutaco.dto.request.BudgetRequest;
import vn.id.luannv.lutaco.dto.response.BudgetResponse;
import vn.id.luannv.lutaco.entity.Budget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "actualAmount", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    Budget toEntity(BudgetRequest request);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    BudgetResponse toDto(Budget budget);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "actualAmount", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    void update(@MappingTarget Budget budget, BudgetRequest request);
}
