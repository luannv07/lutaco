package vn.id.luannv.lutaco.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.entity.RecurringTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    @Query("""
    select rt from RecurringTransaction rt
    where rt.transaction.userId = :userId
    and (:#{#request.frequentType} is null or rt.frequentType = :#{#request.frequentType})
""")
    Page<RecurringTransaction> findByFilters(
            @Param("request") RecurringTransactionFilterRequest request,
            @Param("userId") String userId,
            Pageable pageable
    );

    @Query("""
        select rt from RecurringTransaction rt
        where rt.transaction.userId = :userId
        and rt.id = :id
    """)
    Optional<RecurringTransaction> findByUserIdAndId(@Param("userId") String userId, @Param("id") Long id);

    List<RecurringTransaction> findAllByNextDateBefore(LocalDate date);
}
