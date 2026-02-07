package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.CategoryDto;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.CategoryOverride;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.CategoryMapper;
import vn.id.luannv.lutaco.repository.CategoryOverrideRepository;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.service.CategoryService;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    CategoryOverrideRepository categoryOverrideRepository;

    private List<CategoryDto> getChildrenById(String categoryId) {
        return categoryRepository.findByParentId(categoryId)
                .stream().map(categoryMapper::toDto).toList();
    }
    private CategoryDto buildDto(Category entity) {
        CategoryDto dto = categoryMapper.toDto(entity);
        dto.setChildren(getChildrenById(entity.getId()));
        return dto;
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryDto request) {
        log.info("category create function: {}", request);
        String userId = SecurityUtils.getCurrentId();
        // thêm category hệ thống ma disabled trước đó
        if (categoryOverrideRepository.existsByCategory_CategoryNameAndUserId(request.getCategoryName(), userId)) {
            categoryOverrideRepository.restoreByCategoryName(request.getCategoryName(), userId);
            log.info("category override function: {}", request.getCategoryName());
            Category entity = categoryRepository.findByCategoryNameAndOwnerUserId(request.getCategoryName(), userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

            return buildDto(entity);
        }
        // thêm một category mới hoàn toàn
        Category entity = categoryMapper.toEntity(request);
        categoryRepository.findById(request.getParentId())
                .ifPresent(entity::setParent);
        entity.setOwnerUserId(userId);
        log.info("category create function: {}", request);
        if (categoryRepository.existsByOwnerUserIdAndCategoryName(userId, entity.getCategoryName()))
            throw new BusinessException(ErrorCode.FIELD_EXISTED, Map.of("categoryName", ErrorCode.FIELD_EXISTED.getMessage()));

        return buildDto(categoryRepository.save(entity));
    }

    @Override
    public CategoryDto getDetail(String id) {
        return null;
    }

    @Override
    public Page<CategoryDto> search(CategoryFilterRequest request, Integer page, Integer size) {
        return null;
    }

    @Override
    public List<CategoryDto> searchNoPag(CategoryFilterRequest request) {
        CategoryType categoryType = CategoryType.from(request.getCategoryType());

        List<Category> categoryPage = categoryRepository
                .advancedSearch(request.getCategoryName(), categoryType, SecurityUtils.getCurrentId());

        return categoryPage.stream().map(this::buildDto).toList();
    }

    @Override
    public CategoryDto update(String categoryName, CategoryDto request) {
        Category category = categoryRepository.findByCategoryNameAndOwnerUserId(categoryName, SecurityUtils.getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        categoryMapper.update(category, request);
        categoryRepository.findById(request.getParentId())
                .ifPresent(category::setParent);

        return buildDto(category);
    }

    @Override
    public void deleteById(String categoryName) {
        Category category = categoryRepository.findByCategoryNameAndOwnerUserId(categoryName, SecurityUtils.getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        categoryOverrideRepository
                .save(CategoryOverride.builder()
                        .category(category)
                        .userId(SecurityUtils.getCurrentId())
                        .disabled(true)
                        .build());
    }
}
