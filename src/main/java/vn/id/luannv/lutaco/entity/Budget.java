package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "budgets")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Column(nullable = false, length = 255)
    String name;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    BigDecimal targetAmount;

    @Column(name = "actual_amount", nullable = false, precision = 19, scale = 2)
    BigDecimal actualAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    Period period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    BudgetStatus status;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "percentage", precision = 5, scale = 2)
    BigDecimal percentage;
}