package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.CategoryExpenseProjection;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("""
    select t from Transaction t
    where t.userId = :userId
      and t.deletedAt is null
      and (:#{#request.walletName} is null or t.wallet.walletName = :#{#request.walletName})
      and (:#{#request.categoryName} is null or t.category.categoryName = :#{#request.categoryName})
      and (:#{#request.fromDate} is null or t.transactionDate >= :#{#request.fromDate})
      and (:#{#request.toDate} is null or t.transactionDate <= :#{#request.toDate})
      and (:#{#request.minAmount} is null or t.amount >= :#{#request.minAmount})
      and (:#{#request.maxAmount} is null or t.amount <= :#{#request.maxAmount})
""")
    Page<Transaction> findByFilters(
            @Param("request") TransactionFilterRequest request,
            @Param("userId") String userId,
            Pageable pageable
    );


    @Query("""
    SELECT coalesce(sum(t.amount), 0)
    FROM Transaction t
    WHERE t.userId = :userId
      AND t.category.categoryType = :categoryType
      AND t.deletedAt is null
      AND (
            (t.transactionDate >= :fromDate)
        AND (t.transactionDate <= :toDate)
      )
""")
    Long sumAmountByUser(
            @Param("userId") String userId,
            @Param("categoryType") CategoryType categoryType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
with revenueByCategory as (
    select 
        coalesce(sum(t.amount), 0) as totalEachPart,
        t.category.categoryName as cateName
    from Transaction t
    where t.userId = :userId
      and t.category.categoryType = :categoryType
    group by t.category.categoryName
)
select 
    rbc.totalEachPart as total,
    rbc.cateName as categoryName,
    (rbc.totalEachPart * 1.0 / sum(rbc.totalEachPart) over()) as pct
from revenueByCategory rbc
order by (rbc.totalEachPart * 1.0 / sum(rbc.totalEachPart) over()) desc
""")
    List<CategoryExpenseProjection> getCategoryPercentageOfTotal(
            @Param("userId") String userId,
            @Param("categoryType") CategoryType categoryType
            );



    @Query("""
    select t.category.categoryType from Transaction t
    where t.id = :id
""")
    Object findCategoryTypeById(String id);

    @Query("""
    select t.wallet from Transaction t
    where t.id = :id and t.deletedAt is null
""")
    Optional<Wallet> findWalletWithTransactionId(@Param("id") String id);
}

