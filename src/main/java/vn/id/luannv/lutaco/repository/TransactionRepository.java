package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Transaction;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@Repository
@Transactional
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
    @Query("""
    SELECT t FROM Transaction t
    JOIN FETCH t.category
    JOIN FETCH t.wallet
    WHERE t.id = :id AND t.user.id = :userId AND t.activeFlg = true
""")
    Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
    SELECT t FROM Transaction t
    JOIN FETCH t.category
    JOIN FETCH t.wallet
    WHERE t.id = :id AND t.user.id = :userId
""")
    Optional<Transaction> findByIdAndUserIdIncludingDeleted(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.category.id IN :categoryIds
      AND t.transactionDate >= :startDate
      AND t.transactionDate < :endDate
      AND t.activeFlg = true
""")
    Long sumAmountByCategoryIdsAndDateRange(
            @Param("userId") Long userId,
            @Param("categoryIds") Collection<Long> categoryIds,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
