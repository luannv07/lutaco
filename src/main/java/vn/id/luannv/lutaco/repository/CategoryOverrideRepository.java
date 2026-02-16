package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.CategoryOverride;

import java.util.Optional;

@Repository
public interface CategoryOverrideRepository extends JpaRepository<CategoryOverride, Integer> {
    boolean existsByCategory_CategoryNameAndUserId(String categoryCategoryName, String userId);

    Optional<CategoryOverride> findByCategory_CategoryNameAndUserId(String categoryCategoryName, String userId);

    @Modifying
    @Query("""
                update CategoryOverride co
                set co.disabled = false
                where co.category.categoryName = :name
                  and co.userId = :userId
            """)
    void restoreByCategoryName(@Param("name") String name,
                               @Param("userId") String userId);
}
