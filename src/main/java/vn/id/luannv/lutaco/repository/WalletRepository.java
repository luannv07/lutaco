package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {


    @Modifying
    @Query(value = "UPDATE wallets " +
            "SET current_balance = CASE " +
            "   WHEN :type = 'INCOME' THEN current_balance + :amount " +
            "   WHEN :type = 'EXPENSE' THEN current_balance - :amount " +
            "   ELSE current_balance " +
            "END " +
            "WHERE id = :walletId", nativeQuery = true)
    void updateBalance(@Param("walletId") String walletId, @Param("amount") Long amount, @Param("type") String type);
}
