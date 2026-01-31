package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "category_overrides",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_overrides_user_category",
                        columnNames = {"user_id", "category_id"}
                )
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CategoryOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_category_overrides_category")
    )
    Category category;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "disabled", nullable = false)
    @Builder.Default
    Boolean disabled = true;
}
