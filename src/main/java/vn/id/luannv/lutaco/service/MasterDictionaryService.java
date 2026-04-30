package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.request.MasterDictionaryFilterRequest;
import vn.id.luannv.lutaco.dto.request.MasterDictionaryRequest;
import vn.id.luannv.lutaco.dto.response.MasterDictionaryResponse;

import java.util.List;

public interface MasterDictionaryService
        extends BaseService<MasterDictionaryFilterRequest, MasterDictionaryResponse, MasterDictionaryRequest, Long> {

    List<MasterDictionaryResponse> getByGroup(String dictGroup, Boolean activeFlg);
}
