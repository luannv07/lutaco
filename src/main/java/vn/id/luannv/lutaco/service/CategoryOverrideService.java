package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;

public interface CategoryOverrideService {
    void createOverride(CategoryDto categoryDto);
}
