package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Budget;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.Period;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {

    int countBudgetByUser(User user);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findByUserId(Long userId);

    boolean existsByUserIdAndCategoryIdAndPeriod(Long userId, Long categoryId, Period period);

    boolean existsByUserIdAndCategoryIdAndPeriodAndIdNot(Long userId, Long categoryId, Period period, Long id);
}
