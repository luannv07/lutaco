package vn.id.luannv.lutaco.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    CategoryOverrideRepository categoryOverrideRepository;
    LocalizationUtils localizationUtils;

    @Override
    public List<CategoryResponse> getChildren(String categoryId) {
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
        } else {
            dto.setCategoryType(null);
        }

        return dto;
    }

    @CacheEvict(value = "category_detail", allEntries = true)
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
    @Cacheable(
            value = "category_detail",
            key = "#id + '_' + @securityUtils.getCurrentId() + '_' + @localizationUtils.getCurrentLocaleKey()"
    )
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
    @Transactional(readOnly = true)
    public Page<CategoryResponse> search(CategoryFilterRequest request, Integer page, Integer size) {

        boolean hasSearch = StringUtils.hasText(request.getCategoryName())
                || StringUtils.hasText(request.getCategoryType());

        Pageable pageable = PageRequest.of(page, size);

        List<Category> categories = categoryRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getCategoryName())) {
                predicates.add(cb.like(
                        root.get("categoryName"),
                        "%" + request.getCategoryName() + "%"
                ));
            }

            if (StringUtils.hasText(request.getCategoryType())) {
                predicates.add(cb.equal(root.get("categoryType"), request.getCategoryType()));
            }

            if (query != null && query.getResultType() != Long.class) {
                query.orderBy(
                        cb.desc(root.get("createdDate")),
                        cb.desc(root.get("id"))
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });

        if (!hasSearch) {
            List<CategoryResponse> roots = categories.stream()
                    .filter(c -> c.getParent() == null)
                    .map(this::buildDto)
                    .toList();

            return new PageImpl<>(roots, pageable, roots.size());
        }

        // Split roots & children
        List<Category> roots = categories.stream()
                .filter(c -> c.getParent() == null)
                .toList();

        List<Category> children = categories.stream()
                .filter(c -> c.getParent() != null)
                .toList();

        // Group children by parentId
        Map<String, List<Category>> childrenByParentId = children.stream()
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        Set<String> rootIds = roots.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        Set<String> addedIds = new HashSet<>();
        List<CategoryResponse> responses = new ArrayList<>();

        // 1. Handle roots (with or without children)
        for (Category root : roots) {
            CategoryResponse dto = buildDto(root);
            addedIds.add(root.getId());

            List<Category> childList = childrenByParentId.get(root.getId());
            if (childList != null) {
                dto.setChildren(childList.stream()
                        .map(this::buildDto)
                        .toList());
            }

            responses.add(dto);
        }

        // 2. Handle children whose parents are NOT in roots
        Set<String> parentIdsFromChildren = children.stream()
                .map(c -> c.getParent().getId())
                .collect(Collectors.toSet());

        Set<String> missingParentIds = parentIdsFromChildren.stream()
                .filter(pid -> !rootIds.contains(pid))
                .collect(Collectors.toSet());

        if (!missingParentIds.isEmpty()) {
            List<Category> missingParents = categoryRepository.findAllById(missingParentIds);

            for (Category parent : missingParents) {
                CategoryResponse dto = buildDto(parent);
                addedIds.add(parent.getId());

                List<Category> childList = childrenByParentId.get(parent.getId());
                if (childList != null) {
                    dto.setChildren(childList.stream()
                            .map(this::buildDto)
                            .toList());
                }

                responses.add(dto);
            }
        }

        // 3. Pagination in memory
        int total = responses.size();
        int from = Math.min(pageable.getPageNumber() * pageable.getPageSize(), total);
        int to = Math.min(from + pageable.getPageSize(), total);

        List<CategoryResponse> pageContent = responses.subList(Math.max(from - 1, 0), to);

        return new PageImpl<>(pageContent, pageable, total);
    }

    @CacheEvict(value = "category_detail", allEntries = true)
    @Override
    @Transactional
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
    @CacheEvict(value = "category_detail", allEntries = true)
    @Override
    @Transactional
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
