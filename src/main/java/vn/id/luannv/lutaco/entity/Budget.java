package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.BudgetStatus;
import vn.id.luannv.lutaco.enumerate.Period;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "budgets", indexes = {
        @Index(name = "idx_budget_user_category_period", columnList = "user_id, category_id")
})
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Budget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_category"))
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_user"))
    User user;

    @Column(name = "target_amount", nullable = false)
    Long targetAmount;

    @Column(name = "actual_amount", nullable = false)
    @Builder.Default
    Long actualAmount = 0L;

    @Column(name = "percentage", nullable = false)
    @Builder.Default
    Float percentage = 0.0f;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false)
    Period period;

    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    BudgetStatus status = BudgetStatus.IN_PROGRESS;
}
