package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import vn.id.luannv.lutaco.enumerate.Period;

import java.time.LocalDate;

@Entity
@Table(name = "budgets",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_budget_user_category_deleted", columnNames = {"user_id", "category_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Column(nullable = false)
    String name;

    @Column(name = "target_amount", nullable = false)
    Long targetAmount;

    @Column(name = "actual_amount", nullable = false)
    Long actualAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    Period period;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;
}
