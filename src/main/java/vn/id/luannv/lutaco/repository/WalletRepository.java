package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Wallet;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

    long countByUser_Id(String userId);

    List<Wallet> findByUser_Id(String userId);

    /**
     * and :amount > 0
     * and (
     * :type = 'INCOME'
     * or (:type = 'EXPENSE' and w.currentBalance >= :amount)
     * )
     * Cho phép trừ âm, nó là app wallet ko phải app ngân hàng
     */
    @Modifying
    @Query("""
                    update Wallet w
                    set w.currentBalance =
                        case 
                            when :type = 'EXPENSE' then w.currentBalance - :amount
                            when :type = 'INCOME' then w.currentBalance + :amount
                        end
                    where w.id = :walletId
            """)
    void updateBalance(
            @Param("walletId") String walletId,
            @Param("amount") Long amount,
            @Param("type") String type
    );

    Optional<Wallet> findByUser_IdAndWalletName(String userId, String walletName);

    Optional<Wallet> findByUser_IdAndId(String userId, String id);
}
