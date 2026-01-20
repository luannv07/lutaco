package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.MasterDictionaryDto;

import java.util.List;

public interface MasterDictionaryService {

    List<MasterDictionaryDto> getByCategory(String category);

    MasterDictionaryDto getByCategoryAndCode(String category, String code);

    MasterDictionaryDto create(MasterDictionaryDto dto);

    MasterDictionaryDto update(Integer id, MasterDictionaryDto dto);
}
