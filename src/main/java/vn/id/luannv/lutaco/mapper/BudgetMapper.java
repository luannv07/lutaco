package vn.id.luannv.lutaco.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.id.luannv.lutaco.dto.BudgetDTO;
import vn.id.luannv.lutaco.entity.Budget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "user.username", target = "username")
    BudgetDTO toDTO(Budget budget);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "actualAmount", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Budget toEntity(BudgetDTO budgetDTO);

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
    void updateFromDTO(BudgetDTO budgetDTO, @MappingTarget Budget budget);
}
