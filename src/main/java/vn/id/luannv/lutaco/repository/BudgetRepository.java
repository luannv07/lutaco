package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.User;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {
    @Query("""
    select count(1) from Budget b where b.user = :user
""")
    long countByUser(@Param("user") User user);

    @Query("""
    select b from Budget b where b.user = :user and b.category = :category
""")
    Optional<Budget> existedByUserAndCategory(@Param("user") User user, @Param("category") Category category);
}
