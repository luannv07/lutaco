package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.luannv.lutaco.dto.request.MasterDictionaryFilterRequest;
import vn.id.luannv.lutaco.dto.request.MasterDictionaryRequest;
import vn.id.luannv.lutaco.dto.response.MasterDictionaryResponse;
import vn.id.luannv.lutaco.entity.MasterDictionary;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.repository.MasterDictionaryRepository;
import vn.id.luannv.lutaco.service.MasterDictionaryService;
import vn.id.luannv.lutaco.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MasterDictionaryServiceImpl implements MasterDictionaryService {

    MasterDictionaryRepository masterDictionaryRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "masterDictionaryById", allEntries = true),
            @CacheEvict(value = "masterDictionaryByGroup", allEntries = true)
    })
    public MasterDictionaryResponse create(MasterDictionaryRequest request) {
        String normalizedGroup = StringUtils.normalizeCode(request.getDictGroup());
        String normalizedCode = StringUtils.normalizeCode(request.getCode());

        if (masterDictionaryRepository.existsByDictGroupAndCode(normalizedGroup, normalizedCode)) {
            throw new BusinessException(
                    ErrorCode.ENTITY_EXISTED,
                    Map.of("field", "dict_group,code"));
        }

        MasterDictionary entity = new MasterDictionary();
        entity.setDictGroup(normalizedGroup);
        entity.setCode(normalizedCode);
        entity.setValueVi(request.getValueVi().trim());
        entity.setValueEn(request.getValueEn().trim());
        entity.setActiveFlg(Objects.requireNonNullElse(request.getActiveFlg(), Boolean.TRUE));
        entity.setDisplayOrder(request.getDisplayOrder());

        return toResponse(masterDictionaryRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "masterDictionaryById", key = "#id")
    public MasterDictionaryResponse getDetail(Long id) {
        return toResponse(getByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MasterDictionaryResponse> search(MasterDictionaryFilterRequest request, Integer page, Integer size) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(
                pageIndex,
                size,
                Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("id")));

        Specification<MasterDictionary> specification = Specification.anyOf();

        if (StringUtils.hasText(request.getDictGroup())) {
            specification = specification.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("dictGroup")),
                    "%" + request.getDictGroup().trim().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(request.getCode())) {
            specification = specification.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("code")),
                    "%" + request.getCode().trim().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(request.getValueVi())) {
            specification = specification.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("valueVi")),
                    "%" + request.getValueVi().trim().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(request.getValueEn())) {
            specification = specification.and((root, query, cb) -> cb.like(
                    cb.lower(root.get("valueEn")),
                    "%" + request.getValueEn().trim().toLowerCase() + "%"));
        }

        if (request.getActiveFlg() != null) {
            specification = specification
                    .and((root, query, cb) -> cb.equal(root.get("activeFlg"), request.getActiveFlg()));
        }

        return masterDictionaryRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "masterDictionaryById", allEntries = true),
            @CacheEvict(value = "masterDictionaryByGroup", allEntries = true)
    })
    public MasterDictionaryResponse update(Long id, MasterDictionaryRequest request) {
        MasterDictionary entity = getByIdOrThrow(id);
        String normalizedGroup = StringUtils.normalizeCode(request.getDictGroup());
        String normalizedCode = StringUtils.normalizeCode(request.getCode());

        if (masterDictionaryRepository.existsByDictGroupAndCodeAndIdNot(normalizedGroup, normalizedCode, id)) {
            throw new BusinessException(
                    ErrorCode.ENTITY_EXISTED,
                    Map.of("field", "dict_group,code"));
        }

        entity.setDictGroup(normalizedGroup);
        entity.setCode(normalizedCode);
        entity.setValueVi(request.getValueVi().trim());
        entity.setValueEn(request.getValueEn().trim());
        entity.setActiveFlg(Objects.requireNonNullElse(request.getActiveFlg(), Boolean.TRUE));
        entity.setDisplayOrder(request.getDisplayOrder());

        return toResponse(masterDictionaryRepository.save(entity));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "masterDictionaryById", allEntries = true),
            @CacheEvict(value = "masterDictionaryByGroup", allEntries = true)
    })
    public void deleteById(Long id) {
        MasterDictionary entity = getByIdOrThrow(id);
        masterDictionaryRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "masterDictionaryByGroup", key = "(#dictGroup == null ? '' : #dictGroup.trim().toUpperCase()) + '_' + (#activeFlg == null ? 'ALL' : #activeFlg)")
    public List<MasterDictionaryResponse> getByGroup(String dictGroup, Boolean activeFlg) {
        String normalizedGroup = StringUtils.normalizeCode(dictGroup);

        List<MasterDictionary> items = activeFlg == null
                ? masterDictionaryRepository.findByDictGroupOrderByDisplayOrderAscIdAsc(normalizedGroup)
                : masterDictionaryRepository.findByDictGroupAndActiveFlgOrderByDisplayOrderAscIdAsc(normalizedGroup,
                        activeFlg);

        return items.stream().map(this::toResponse).toList();
    }

    private MasterDictionary getByIdOrThrow(Long id) {
        return masterDictionaryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        Map.of("field", "master_dictionary.id")));
    }

    private MasterDictionaryResponse toResponse(MasterDictionary entity) {
        return MasterDictionaryResponse.builder()
                .id(entity.getId())
                .dictGroup(entity.getDictGroup())
                .code(entity.getCode())
                .valueVi(entity.getValueVi())
                .valueEn(entity.getValueEn())
                .activeFlg(entity.getActiveFlg())
                .displayOrder(entity.getDisplayOrder())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .updatedBy(entity.getUpdatedBy())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

}
