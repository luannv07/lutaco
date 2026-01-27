package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.luannv.lutaco.entity.Budget;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, String> {

    long countByUser_Id(String userId);

    List<Budget> findByUser_Id(String userId);

    Optional<Budget> findByUser_IdAndBudgetName(String userId, String budgetName);
}
