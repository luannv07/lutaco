package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "user_id", nullable = false, updatable = false)
    String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transactions_category")
    )
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "wallet_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transactions_wallet")
    )
    Wallet wallet;

    @Column(nullable = false)
    Long amount;

    @Column(name = "transaction_date", nullable = false)
    LocalDateTime transactionDate;

    @Column(length = 255)
    String note;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}
