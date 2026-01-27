package vn.id.luannv.lutaco.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.entity.Budget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentBalance", source = "initialBalance")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Budget toEntity(BudgetCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "initialBalance", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void update(@MappingTarget Budget budget, BudgetUpdateRequest request);
}
