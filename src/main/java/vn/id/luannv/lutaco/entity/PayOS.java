package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.id.luannv.lutaco.enumerate.PaymentStatus;
import vn.id.luannv.lutaco.enumerate.PaymentType;

import java.math.BigInteger;
import java.time.Instant;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Pay_OS")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PayOS extends BaseEntity {

    @Column(name = "ORDER_CODE", nullable = false, unique = true)
    Integer orderCode;

    @Column(name = "payment_link_id", unique = true)
    String paymentLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    User user;

    @Column(name = "AMOUNT", nullable = false)
    BigInteger amount;

    @Column(name = "CURRENCY", nullable = false)
    String currency;

    @Column(name = "DESCRIPTION")
    String description;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    PaymentType type;

    @Column(name = "PAID_AT")
    Instant paidAt;

    @PreUpdate
    public void preUpdate() {
        if (this.status == PaymentStatus.PAID) {
            Instant now = Instant.now();
            log.info("PayOS preUpdate Payment request: {} at {}", this.status, now);
            this.paidAt = now;
        }
    }
}