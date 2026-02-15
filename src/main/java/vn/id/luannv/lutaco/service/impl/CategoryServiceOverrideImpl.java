package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.service.CategoryOverrideService;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryServiceOverrideImpl implements CategoryOverrideService {

    @Override
    public void createOverride(CategoryDto categoryDto) {

    }
}
