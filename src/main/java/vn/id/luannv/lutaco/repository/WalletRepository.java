package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    int countWalletByUser(User user);
}
