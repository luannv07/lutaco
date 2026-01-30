package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.mapper.CategoryMapper;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.service.CategoryOverrideService;
import vn.id.luannv.lutaco.service.CategoryService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryServiceOverrideImpl implements CategoryOverrideService {

    @Override
    public void createOverride(CategoryDto categoryDto) {

    }
}
