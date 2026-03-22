package vn.id.luannv.lutaco.service.impl;

import jakarta.transaction.Transactional;
import liquibase.util.StringUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.resource.CachingResourceResolver;
import vn.id.luannv.lutaco.dto.EnumDisplay;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.dto.request.CategoryRequest;
import vn.id.luannv.lutaco.dto.response.CategoryResponse;
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
import vn.id.luannv.lutaco.util.LocalizationUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.time.LocalDateTime;
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
    LocalizationUtils localizationUtils;

    private List<CategoryResponse> getChildrenById(String categoryId) {
        return categoryRepository.findByParentId(categoryId)
                .stream().map(this::buildDto).toList();
    }

    private CategoryResponse buildDto(Category entity) {
        CategoryResponse dto = categoryMapper.toDto(entity);

        if (entity.getParent() == null) {
            dto.setCategoryType(new EnumDisplay<>(
                    entity.getCategoryType(),
                    localizationUtils.getLocalizedMessage(entity.getCategoryType().getDisplay())
            ));
            List<CategoryResponse> children = getChildrenById(entity.getId());
            dto.setChildren(children.isEmpty() ? null : children);
        } else {
            dto.setCategoryType(null);
            dto.setChildren(null);
        }

        return dto;
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        String userId = SecurityUtils.getCurrentId();
        log.info("[{}]: Attempting to create category for user ID: {}. Request: {}", username, userId, request.getCategoryName());

        // Nếu đã tồn tại override (system category bị disable) → restore
        if (categoryOverrideRepository.existsByCategory_CategoryNameAndUserId(request.getCategoryName(), userId)) {
            Category existingCategory = categoryRepository
                    .findByCategoryNameAndOwnerUserId(request.getCategoryName(), userId)
                    .orElseThrow(() -> {
                        log.error("[{}]: Category '{}' not found during restore attempt.", username, request.getCategoryName());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                    });

            // Nếu type khác thì không restore được, báo lỗi
            if (request.getCategoryType() != null) {
                CategoryType requestedType = EnumUtils.from(CategoryType.class, request.getCategoryType());
                if (!existingCategory.getCategoryType().equals(requestedType)) {
                    log.warn("[{}]: Category '{}' exists with type '{}', requested type '{}'.",
                            username, request.getCategoryName(), existingCategory.getCategoryType(), requestedType);
                    throw new BusinessException(ErrorCode.FIELD_EXISTED,
                            Map.of("reason", "Category with same name exists with different type, please use a different name"));
                }
            }

            categoryOverrideRepository.restoreByCategoryName(request.getCategoryName(), userId);
            log.info("[{}]: Restored previously disabled system category '{}' for user ID {}.", username, request.getCategoryName(), userId);
            return buildDto(existingCategory);
        }

        Category entity = categoryMapper.toEntity(request);

        // NOTE: categoryType là bất biến sau khi tạo.
        // Child inherit type từ parent, root category tự chọn khi tạo.
        // Admin KHÔNG được đổi type của system category
        // vì sẽ gây inconsistent với toàn bộ custom child và transaction liên quan.
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> {
                        log.warn("[{}]: [create] Parent category ID '{}' not found.", username, request.getParentId());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND, Map.of("parentId", request.getParentId()));
                    });
            entity.setParent(parent);
            entity.setCategoryType(parent.getCategoryType()); // inherit từ parent
            log.debug("[{}]: Category '{}' inherits type '{}' from parent ID '{}'.",
                    username, request.getCategoryName(), parent.getCategoryType(), parent.getId());
        } else {
            if (request.getCategoryType() == null) {
                throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING,
                        Map.of("categoryType", "Category type is required for root category"));
            }
            entity.setCategoryType(EnumUtils.from(CategoryType.class, request.getCategoryType()));
        }

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
    public CategoryResponse getDetail(String id) {
        String username = SecurityUtils.getCurrentUsername();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Category with ID '{}' not found.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        SecurityUtils.assertOwnerOrAdmin(category.getOwnerUserId());
        return buildDto(category);
    }

    @Override
    public Page<CategoryResponse> search(CategoryFilterRequest request, Integer page, Integer size) {
        throw new UnsupportedOperationException("Use searchNoPag instead");
    }

    @Override
    public List<CategoryResponse> searchNoPag(CategoryFilterRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        String userId = SecurityUtils.getCurrentId();
        log.info("[{}]: Searching categories for user ID: {} with filter: {}.", username, userId, request);
        CategoryType categoryType = null;
        if (StringUtils.hasText(request.getCategoryType())) {
            categoryType=EnumUtils.from(CategoryType.class, request.getCategoryType());
        }

        List<Category> categories = categoryRepository.advancedSearch(request.getCategoryName(), categoryType, userId);

        log.info("[{}]: Found {} categories for user ID {} matching the criteria.", username, categories.size(), userId);
        return categories.stream().map(this::buildDto).toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", key = "#id + @securityPermission.getCurrentUserId()")
    public CategoryResponse update(String id, CategoryRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to update category ID: {}. Request: {}", username, id, request);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Category with ID '{}' not found for update.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        if (category.getIsSystem()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }
        SecurityUtils.assertOwnerOrAdmin(category.getOwnerUserId());

        categoryMapper.update(category, request); // mapper đã ignore categoryType

        // ko thể update parent, cate type

        Category updatedCategory = categoryRepository.save(category);
        log.info("[{}]: Category ID '{}' updated successfully.", username, updatedCategory.getId());
        return buildDto(updatedCategory);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", key = "#id + @securityPermission.getCurrentUserId()")
    public void deleteById(String id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("[{}]: Attempting to delete category ID: {}.", username, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[{}]: Category with ID '{}' not found for deletion.", username, id);
                    return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                });

        if (category.getIsSystem()) {
            // System category → disable qua override, giữ lại cho transaction reference
            categoryOverrideRepository.save(CategoryOverride.builder()
                    .category(category)
                    .userId(SecurityUtils.getCurrentId())
                    .disabled(true)
                    .build());
            log.info("[{}]: System category ID '{}' disabled via override.", username, id);
        } else {
            // Custom category → soft delete, giữ lại cho transaction reference
            SecurityUtils.assertOwnerOrAdmin(category.getOwnerUserId());
            category.setDeletedAt(LocalDateTime.now());
            categoryRepository.save(category);
            log.info("[{}]: Custom category ID '{}' soft deleted.", username, id);
        }
    }
}