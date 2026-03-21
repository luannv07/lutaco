package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Wallet;
import vn.id.luannv.lutaco.enumerate.WalletStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    long countByUser_IdAndStatus(String userId, WalletStatus status);

    long countByUser_Id(String userId);

    Optional<Wallet> findByUser_IdAndWalletName(String userId, String walletName);

    Optional<Wallet> findByUser_IdAndWalletNameAndStatus(String userId, String walletName, WalletStatus status);

    Optional<Wallet> findByUser_IdAndIdAndStatus(String userId, String id, WalletStatus status);

    Optional<Wallet> findByUser_IdAndId(String userId, String id);

    List<Wallet> findByUser_IdAndStatus(String userId, WalletStatus status);

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
