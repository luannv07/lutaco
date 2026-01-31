package vn.id.luannv.lutaco.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.entity.Transaction;

@Repository
@Transactional
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("""
        select t from Transaction t
        where t.user.id = :userId
          and t.deletedAt is null
          and (:#{#request.categoryId} is null or t.category.id = :#{#request.categoryId})
          and (:#{#request.transactionType} is null or t.transactionType = :#{#request.transactionType})
          and (:#{#request.fromDate} is null or t.transactionDate >= :#{#request.fromDate})
          and (:#{#request.toDate} is null or t.transactionDate <= :#{#request.toDate})
          and (:#{#request.minAmount} is null or t.amount >= :#{#request.minAmount})
          and (:#{#request.maxAmount} is null or t.amount <= :#{#request.maxAmount})
        order by t.transactionDate desc
    """)
    Page<Transaction> findByFilters(
            @Param("request") TransactionFilterRequest request,
            @Param("userId") String userId,
            Pageable pageable
    );
}
