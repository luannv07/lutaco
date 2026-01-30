package vn.id.luannv.lutaco.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.CategoryOverride;

import java.awt.print.PrinterGraphics;
import java.util.List;
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
    int restoreByCategoryName(@Param("name") String name,
                              @Param("userId") String userId);
}
