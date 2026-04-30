package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.dto.request.CategoryRequest;
import vn.id.luannv.lutaco.dto.response.CategoryResponse;
import vn.id.luannv.lutaco.service.CategoryService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {

    @Override
    public List<CategoryResponse> getChildren(String categoryId) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public CategoryResponse getDetail(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> search(CategoryFilterRequest request, Integer page, Integer size) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public CategoryResponse update(String id, CategoryRequest request) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
