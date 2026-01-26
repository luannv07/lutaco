package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.luannv.lutaco.entity.PayOS;
import vn.id.luannv.lutaco.enumerate.PaymentStatus;
import vn.id.luannv.lutaco.enumerate.PaymentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PayOSRepository extends JpaRepository<PayOS, Long> {
    Optional<PayOS> getPayOSByOrderCode(Integer orderCode);

    @Query(value = "select p.user_id from pay_os p where p.order_code = :orderCode", nativeQuery = true)
    String getUserIdByOrderCode(@Param("orderCode") Integer orderCode);

    Optional<PayOS> findFirstByOrderByOrderCodeDesc();

    @Modifying
    @Query("update PayOS p set p.status = :statusTarget where p.type = :type and p.orderCode = :orderCode and p.status = :statusSource")
    int updatePayOsStatus(@Param("statusTarget") PaymentStatus target,
                          @Param("type") PaymentType paymentType,
                          @Param("orderCode") Integer orderCode,
                          @Param("statusSource") PaymentStatus source);

    List<PayOS> findByStatusAndPaidAtIsNullAndCreatedDateIsLessThan(PaymentStatus paymentStatus, LocalDateTime localDateTime);
}
