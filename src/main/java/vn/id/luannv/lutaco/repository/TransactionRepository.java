package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.projection.CategoryExpenseProjection;
import vn.id.luannv.lutaco.dto.projection.RecurringTransactionProjection;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.entity.Transaction;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.CategoryType;
import vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent;

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

    @Query(value = """
    with totalByParent as (
        select if(c.parent_id is null, c.id, c.parent_id) as groupId, sum(t.amount) as eachTotal
        from transactions t
        join categories c on c.id = t.category_id
        where t.user_id = :userId
          and c.category_type = :categoryType
          and t.transaction_date >= :fromDate
          and t.transaction_date <=  :toDate
        group by groupId
    )
    select
        c.category_name categoryParentName,
        tbp.groupId,
        tbp.eachTotal total,
        (tbp.eachTotal / sum(tbp.eachTotal) over ()) pct
    from totalByParent tbp
    join categories c on c.id = tbp.groupId
    group by tbp.groupId, tbp.eachTotal, c.category_name
""", nativeQuery = true)
    List<CategoryExpenseProjection> getCategoryPercentageOfTotal(
            @Param("userId") String userId,
            @Param("categoryType") String categoryType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );



    @Query("""
    select t.category.categoryType from Transaction t
    where t.id = :id
""")
    Object findCategoryTypeById(@Param("id") String id);

    @Query("""
    select t.wallet from Transaction t
    where t.id = :id and t.deletedAt is null
""")
    Optional<Wallet> findWalletWithTransactionId(@Param("id") String id);

    @Query(value = """
    select new vn.id.luannv.lutaco.event.entity.RecurringTransactionEvent$RecurringUserFields(
        t.id,
        t.note,
        t.wallet.id,
        t.wallet.walletName,
        u.email,
        u.fullName,
        t.amount,
        t.userId
    )
    from Transaction t
    join User u on u.id = t.userId
    where t.id = :transactionId and t.deletedAt is null
""")
    Optional<RecurringTransactionEvent.RecurringUserFields> getRecurringUserFieldsByTransactionId(@Param("transactionId") String transactionId);

    @Query("""
    select t.category.id categoryId, t.wallet.id walletId, t.category.categoryType categoryType from Transaction t
    where t.id = :id and t.deletedAt is null
""")
    RecurringTransactionProjection findLinkingFieldsById(@Param("id") String id);
}

