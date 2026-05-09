package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.dto.request.CategoryRequest;
import vn.id.luannv.lutaco.dto.response.CategoryResponse;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.CategoryRepository;
import vn.id.luannv.lutaco.service.CategoryService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildren(Long categoryId) {
        Category parent = getByIdOrThrow(categoryId);
        return parent.getChildren()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (StringUtils.hasText(request.getParentId())) {
            Category parent = getByIdOrThrow(Long.parseLong(request.getParentId()));
            Objects.requireNonNull(parent);
        }

        Category entity = new Category();
        entity.setCategoryCode(request.getCategoryName());

        if (StringUtils.hasText(request.getCategoryType())) {
            CategoryType categoryType = EnumUtils.from(CategoryType.class, request.getCategoryType());
            entity.setCategoryType(categoryType);
        }

        if (StringUtils.hasText(request.getParentId())) {
            Category parent = getByIdOrThrow(Long.parseLong(request.getParentId()));
            entity.setParent(parent);
        }

        entity.setActiveFlg(true);
        return toResponse(categoryRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getDetail(Long id) {
        return toResponse(getByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> search(CategoryFilterRequest request, Integer page, Integer size) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Order.asc("categoryCode"), Sort.Order.asc("id")));

        Specification<Category> specification = Specification.anyOf();

        if (StringUtils.hasText(request.getCategoryName())) {
            specification = specification.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("categoryCode")),
                    "%" + request.getCategoryName().trim().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(request.getCategoryType())) {
            try {
                CategoryType categoryType = CategoryType.valueOf(request.getCategoryType().toUpperCase());
                specification = specification.and((root, query, cb) -> cb.equal(root.get("categoryType"), categoryType));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid category type: {}", request.getCategoryType());
            }
        }

        return categoryRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category entity = getByIdOrThrow(id);

        entity.setCategoryCode(request.getCategoryName());

        if (StringUtils.hasText(request.getCategoryType())) {
            CategoryType categoryType = EnumUtils.from(CategoryType.class, request.getCategoryType());
            entity.setCategoryType(categoryType);
        }

        if (StringUtils.hasText(request.getParentId())) {
            Long parentId = Long.parseLong(request.getParentId());
            if (!parentId.equals(entity.getId())) {
                Category parent = getByIdOrThrow(parentId);
                entity.setParent(parent);
            }
        } else {
            entity.setParent(null);
        }

        return toResponse(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Category entity = getByIdOrThrow(id);
        categoryRepository.delete(entity);
    }

    private Category getByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        Map.of("field", "category.id")));
    }

    private CategoryResponse toResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        CategoryResponse response = new CategoryResponse();
        response.setId(entity.getId() != null ? entity.getId().toString() : null);
        response.setCategoryName(entity.getCategoryCode());
        response.setParentId(entity.getParent() != null ? entity.getParent().getId().toString() : null);
        response.setCategoryType(entity.getCategoryType());
        response.setIsSystem(false);
        response.setCreatedDate(entity.getCreatedDate() != null ? 
                java.time.LocalDateTime.ofInstant(entity.getCreatedDate(), java.time.ZoneId.systemDefault()) : null);
        response.setCreatedBy(entity.getCreatedBy());
        response.setHasChildren(!entity.getChildren().isEmpty());

        if (!entity.getChildren().isEmpty()) {
            response.setChildren(entity.getChildren()
                    .stream()
                    .map(this::toResponse)
                    .toList());
        }

        return response;
    }
}
