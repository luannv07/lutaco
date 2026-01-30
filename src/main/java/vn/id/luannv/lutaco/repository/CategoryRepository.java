package vn.id.luannv.lutaco.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.request.CategoryFilterRequest;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    @Query("select c from Category c where c.parent.id = :parentId")
    List<Category> findByParentId(@Param("parentId") String parentId);

    Category findByCategoryName(String categoryName);

    Category findByCategoryNameAndOwnerUserId(String categoryName, String ownerUserId);
    @Query("""
    select c
    from Category c
    left join CategoryOverride co
           on co.category = c
          and co.userId = :userId
    where c.deletedAt is null
      and (co is null or co.disabled = false)
      and (:categoryName is null
           or lower(c.categoryName) like concat('%', lower(:categoryName), '%'))
      and (:categoryType is null
           or c.categoryType = :categoryType)
      and (c.ownerUserId = :userId or c.isSystem = true)
""")
    Page<Category> advancedSearch(
            @Param("categoryName") String categoryName,
            @Param("categoryType") CategoryType categoryType,
            @Param("userId") String userId,
            Pageable pageable
    );
}

