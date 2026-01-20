package vn.id.luannv.lutaco.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import vn.id.luannv.lutaco.dto.MasterDictionaryDto;
import vn.id.luannv.lutaco.entity.MasterDictionary;

import java.util.List;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface MasterDictionaryMapper {
    MasterDictionaryDto toDto(MasterDictionary entity);

    List<MasterDictionaryDto> toDtoList(List<MasterDictionary> entities);

    @Mapping(target = "code", source = "code", qualifiedByName = "upperCase")
    @Mapping(target = "category", source = "category", qualifiedByName = "upperCase")
    @Mapping(target = "id", ignore = true)
    MasterDictionary toEntity(MasterDictionaryDto dto);

    @Mapping(target = "code", source = "code", qualifiedByName = "upperCase")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void updateEntityFromDto(
            MasterDictionaryDto dto,
            @MappingTarget MasterDictionary entity
    );

    @Named("upperCase")
    default String upperCase(String str) {
        return str != null ? str.trim().toUpperCase(Locale.ROOT) : null;
    }
}
