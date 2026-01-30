package vn.id.luannv.lutaco.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.BudgetCreateRequest;
import vn.id.luannv.lutaco.dto.request.BudgetUpdateRequest;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isSystem", ignore = true)
    @Mapping(target = "ownerUserId", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryDto request);

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    CategoryDto toDto(Category entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isSystem", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "ownerUserId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void update(@MappingTarget Category category, CategoryDto request);

}
