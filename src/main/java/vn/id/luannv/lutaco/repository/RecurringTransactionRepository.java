package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.RecurringTransaction;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
}
