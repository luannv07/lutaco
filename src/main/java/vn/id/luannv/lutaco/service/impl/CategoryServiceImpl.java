package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
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
import vn.id.luannv.lutaco.util.EnumUtils;
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
        String username = SecurityUtils.getCurrentUsername();
        String userId = SecurityUtils.getCurrentId();
        log.info("[{}]: Attempting to create category for user ID: {}. Request: {}", username, userId, request.getCategoryName());

        if (categoryOverrideRepository.existsByCategory_CategoryNameAndUserId(request.getCategoryName(), userId)) {
            categoryOverrideRepository.restoreByCategoryName(request.getCategoryName(), userId);
            log.info("[{}]: Restored previously disabled system category '{}' for user ID {}.", username, request.getCategoryName(), userId);
            Category entity = categoryRepository.findByCategoryNameAndOwnerUserId(request.getCategoryName(), userId)
                    .orElseThrow(() -> {
                        log.error("[{}]: Restored category '{}' not found after restoration attempt for user ID {}.", username, request.getCategoryName(), userId);
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                    });
            return buildDto(entity);
        }

        Category entity = categoryMapper.toEntity(request);
        categoryRepository.findById(request.getParentId())
                .ifPresent(entity::setParent);
        entity.setOwnerUserId(userId);

        if (categoryRepository.existsByOwnerUserIdAndCategoryName(userId, entity.getCategoryName())) {
            log.warn("[{}]: Category with name '{}' already exists for user ID {}.", username, entity.getCategoryName(), userId);
            throw new BusinessException(ErrorCode.FIELD_EXISTED, Map.of("categoryName", ErrorCode.FIELD_EXISTED.getMessage()));
        }

        Category savedCategory = categoryRepository.save(entity);
        log.info("[{}]: New category '{}' (ID: {}) created successfully for user ID {}.", username, savedCategory.getCategoryName(), savedCategory.getId(), userId);
        return buildDto(savedCategory);
    }

    @Override
    public CategoryDto getDetail(String id) {
        log.warn("[{}]: Method getDetail(String id) is not implemented for CategoryService. Use searchNoPag for details.", SecurityUtils.getCurrentUsername());
        return null; // Or throw an UnsupportedOperationException
    }

    @Override
    public Page<CategoryDto> search(CategoryFilterRequest request, Integer page, Integer size) {
        log.warn("[{}]: Method search(CategoryFilterRequest request, Integer page, Integer size) is not implemented for CategoryService. Use searchNoPag for details.", SecurityUtils.getCurrentUsername());
        return null; // Or throw an UnsupportedOperationException
    }

    @Override
    @CacheEvict(value = "categories", key = "#categoryName + @securityPermission.getCurrentUserId()")
    public CategoryDto update(String categoryName, CategoryDto request) {
        String username = SecurityUtils.getCurrentUsername();
        String userId = SecurityUtils.getCurrentId();
        log.info("[{}]: Attempting to update category '{}' for user ID: {}. Request: {}", username, categoryName, userId, request);

        Category category = categoryRepository.findByCategoryNameAndOwnerUserId(categoryName, userId)
                .orElseThrow(() -> {
                    log.warn("[{}]: Category with name '{}' not found for user ID {} for update.", username, categoryName, userId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        categoryMapper.update(category, request);
        categoryRepository.findById(request.getParentId())
                .ifPresent(category::setParent);

        Category updatedCategory = categoryRepository.save(category);
        log.info("[{}]: Category '{}' (ID: {}) updated successfully for user ID {}.", username, updatedCategory.getCategoryName(), updatedCategory.getId(), userId);
        return buildDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> searchNoPag(CategoryFilterRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        String userId = SecurityUtils.getCurrentId();
        log.info("[{}]: Searching categories for user ID: {} with filter: {}.", username, userId, request);
        CategoryType categoryType = EnumUtils.from(CategoryType.class, request.getCategoryType());

        List<Category> categories = categoryRepository
                .advancedSearch(request.getCategoryName(), categoryType, userId);

        log.info("[{}]: Found {} categories for user ID {} matching the criteria.", username, categories.size(), userId);
        return categories.stream().map(this::buildDto).toList();
    }

    @Override
    @CacheEvict(value = "categories", key = "#categoryName + @securityPermission.getCurrentUserId()")
    public void deleteById(String categoryName) {
        String username = SecurityUtils.getCurrentUsername();
        String userId = SecurityUtils.getCurrentId();
        log.info("[{}]: Attempting to soft delete category '{}' for user ID: {}.", username, categoryName, userId);

        Category category = categoryRepository.findByCategoryNameAndOwnerUserId(categoryName, userId)
                .orElseThrow(() -> {
                    log.warn("[{}]: Category with name '{}' not found for user ID {} for deletion.", username, categoryName, userId);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        categoryOverrideRepository
                .save(CategoryOverride.builder()
                        .category(category)
                        .userId(userId)
                        .disabled(true)
                        .build());
        log.info("[{}]: Category '{}' (ID: {}) soft deleted successfully for user ID {}.", username, category.getCategoryName(), category.getId(), userId);
    }
}
