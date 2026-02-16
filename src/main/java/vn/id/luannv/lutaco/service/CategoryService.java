package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;

import java.util.List;

public interface CategoryService extends BaseService<CategoryFilterRequest, CategoryDto, CategoryDto, String> {
    List<CategoryDto> searchNoPag(CategoryFilterRequest request);
}
