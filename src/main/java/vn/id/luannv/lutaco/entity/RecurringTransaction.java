package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.FrequentType;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "recurring_transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringTransaction extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "transaction_id",
            nullable = false,
            unique = true
    )
    Transaction transaction;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "next_date")
    LocalDate nextDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequent_type", nullable = false, length = 50)
    FrequentType frequentType;

    @Column(name = "active_flg", nullable = false)
    boolean activeFlg = true;
}