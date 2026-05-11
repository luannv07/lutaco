package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.projection.CategoryAmountProjection;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
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

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.category.categoryType = :type
      AND t.transactionDate >= :startDate
      AND t.transactionDate < :endDate
      AND t.activeFlg = true
""")
    Long sumAmountByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
    SELECT COALESCE(parent.categoryCode, c.categoryCode) as categoryName,
           COALESCE(SUM(t.amount), 0) as total
    FROM Transaction t
    JOIN t.category c
    LEFT JOIN c.parent parent
    WHERE t.user.id = :userId
      AND c.categoryType = vn.id.luannv.lutaco.enumerate.CategoryType.EXPENSE
      AND t.transactionDate >= :startDate
      AND t.transactionDate < :endDate
      AND t.activeFlg = true
    GROUP BY COALESCE(parent.categoryCode, c.categoryCode)
    ORDER BY SUM(t.amount) DESC
""")
    List<CategoryAmountProjection> findTopExpenseCategoriesByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    List<Transaction> findByIdIn(Collection<Long> ids);

    List<Transaction> findByIdInAndUserId(List<Long> list, Long currentId);
}
