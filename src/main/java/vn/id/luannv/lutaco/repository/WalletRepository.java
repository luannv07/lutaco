package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.entity.Wallet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    int countWalletByUser(User user);

    Optional<Wallet> findByIdAndUserId(Long id, Long currentId);

    List<Wallet> findByUserId(Long userId);

    @Modifying
    @Query("""
    UPDATE Wallet w
    SET w.balance = w.balance + CASE WHEN :type = 'EXPENSE' THEN (-1 * :amount) ELSE :amount END
    WHERE w.id = :walletId
      AND (:type <> 'EXPENSE' OR w.balance >= :amount)
""")
    int updateBalance(
            @Param("walletId") Long walletId,
            @Param("amount") Long amount,
            @Param("type") String type
    );

    List<Wallet> findAllByIdInAndUserId(Collection<Long> ids, Long userId);
}
