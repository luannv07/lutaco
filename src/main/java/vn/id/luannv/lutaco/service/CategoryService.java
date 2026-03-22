package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.dto.request.CategoryRequest;
import vn.id.luannv.lutaco.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService extends BaseService<CategoryFilterRequest, CategoryResponse, CategoryRequest, String> {
    List<CategoryResponse> searchNoPag(CategoryFilterRequest request);
}
