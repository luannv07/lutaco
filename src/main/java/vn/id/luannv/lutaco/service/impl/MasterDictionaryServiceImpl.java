package vn.id.luannv.lutaco.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.MasterDictionaryDto;
import vn.id.luannv.lutaco.entity.MasterDictionary;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.mapper.MasterDictionaryMapper;
import vn.id.luannv.lutaco.repository.MasterDictionaryRepository;
import vn.id.luannv.lutaco.service.MasterDictionaryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MasterDictionaryServiceImpl implements MasterDictionaryService {

    MasterDictionaryRepository repository;
    MasterDictionaryMapper mapper;

    @Override
    public List<MasterDictionaryDto> getByCategory(String category) {
        return mapper.toDtoList(
                repository.findByCategoryAndIsActiveTrue(category)
        );
    }

    @Override
    public MasterDictionaryDto getByCategoryAndCode(String category, String code) {
        MasterDictionary entity = repository
                .findByCategoryAndCode(category.toUpperCase(), code.toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return mapper.toDto(entity);
    }

    @Override
    public MasterDictionaryDto create(MasterDictionaryDto dto) {
        MasterDictionary entity = mapper.toEntity(dto);
        entity.setIsActive(true);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public MasterDictionaryDto update(Integer id, MasterDictionaryDto dto) {
        MasterDictionary entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        mapper.updateEntityFromDto(dto, entity);
        if (entity.getCode() != null)
            entity.setCode(dto.getCode().toUpperCase());
        return mapper.toDto(repository.save(entity));
    }
}
