package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.id.luannv.lutaco.enumerate.PaymentStatus;
import vn.id.luannv.lutaco.enumerate.PaymentType;

import java.time.LocalDateTime;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    Long id;

    @Column(name = "ORDER_CODE", nullable = false, unique = true)
    Integer orderCode;

    @Column(name = "payment_link_id", unique = true)
    String paymentLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    User user;

    @Column(name = "AMOUNT", nullable = false)
    Integer amount;

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
    LocalDateTime paidAt;

    @PreUpdate
    public void preUpdate() {
        if (this.status == PaymentStatus.PAID) {
            LocalDateTime now = LocalDateTime.now();
            log.info("PayOsClient preUpdate Payment request: {} at {}", this.status, now);
            this.paidAt = now;
        }
    }
}
