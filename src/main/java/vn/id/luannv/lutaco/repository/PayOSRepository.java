package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.luannv.lutaco.entity.PayOS;

import java.util.Optional;

public interface PayOSRepository extends JpaRepository<PayOS, Long> {
    Optional<PayOS> getPayOSByOrderCode(Integer orderCode);

    @Query(value = "select p.user_id from pay_os p where p.order_coe = :orderCode", nativeQuery = true)
    String getUserIdByOrderCode(@Param("orderCode") Integer orderCode);
}
