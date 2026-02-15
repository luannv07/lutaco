package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.FrequentType;

import java.time.LocalDate;

@Entity
@Table(name = "recurring_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "transaction_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_recurring_transactions_transaction")
    )
    Transaction transaction;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "next_date")
    LocalDate nextDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequent_type", nullable = false)
    FrequentType frequentType;
}
