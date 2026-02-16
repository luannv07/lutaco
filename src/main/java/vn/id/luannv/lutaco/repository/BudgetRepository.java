package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.Category;
import vn.id.luannv.lutaco.entity.User;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {
    /**
     * Counts the number of budgets associated with a specific user.
     *
     * @param user The user entity.
     * @return The total number of budgets for the user.
     */
    long countByUser(User user);

    @Query("""
                select b from Budget b
                where b.user.id = :userId
                  and b.category.id = :categoryId
                  and :date between b.startDate and b.endDate
            """)
    Optional<Budget> findActiveBudget(
            String userId,
            String categoryId,
            LocalDate date
    );

    boolean existsByUserAndCategory(User user, Category category);
}
