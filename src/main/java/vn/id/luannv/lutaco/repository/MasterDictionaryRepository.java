package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.MasterDictionary;

import java.util.List;
import java.util.Optional;

@Repository

public interface MasterDictionaryRepository extends JpaRepository<MasterDictionary, Integer> {

    List<MasterDictionary> findByCategoryAndIsActiveTrue(String category);

    Optional<MasterDictionary> findByCategoryAndCode(String category, String code);
}
