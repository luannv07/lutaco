package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Wallet;

import java.util.List;
import java.util.Optional;
@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

    long countByUser_Id(String userId);

    List<Wallet> findByUser_Id(String userId);

    Optional<Wallet> findByUser_IdAndWalletName(String userId, String walletName);
}
