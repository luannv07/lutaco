package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.MasterDictionary;

import java.util.List;

@Repository
public interface MasterDictionaryRepository
        extends JpaRepository<MasterDictionary, Long>, JpaSpecificationExecutor<MasterDictionary> {

    boolean existsByDictGroupAndCode(String dictGroup, String code);

    boolean existsByDictGroupAndCodeAndIdNot(String dictGroup, String code, Long id);

    List<MasterDictionary> findByDictGroupOrderByDisplayOrderAscIdAsc(String dictGroup);

    List<MasterDictionary> findByDictGroupAndActiveFlgOrderByDisplayOrderAscIdAsc(String dictGroup, Boolean activeFlg);
}
