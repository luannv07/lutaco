package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.RecurringTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository
        extends JpaRepository<RecurringTransaction, Long>, JpaSpecificationExecutor<RecurringTransaction> {

    @Query("""
            SELECT rt.id FROM RecurringTransaction rt
            WHERE rt.activeFlg = true AND rt.nextDate = :today
            """)
    List<Long> findDueActiveJobIds(@Param("today") LocalDate today);

    @Query("""
            SELECT rt FROM RecurringTransaction rt
            JOIN FETCH rt.transaction t
            JOIN FETCH t.category
            JOIN FETCH t.wallet
            WHERE rt.id = :id AND t.user.id = :userId
            """)
    Optional<RecurringTransaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            SELECT rt FROM RecurringTransaction rt
            JOIN FETCH rt.transaction t
            JOIN FETCH t.category
            JOIN FETCH t.wallet
            WHERE t.user.id = :userId
            ORDER BY rt.createdDate DESC
            """)
    List<RecurringTransaction> findByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(rt) FROM RecurringTransaction rt
            JOIN rt.transaction t
            WHERE t.user.id = :userId
            """)
    int countByUserId(@Param("userId") Long userId);
}
