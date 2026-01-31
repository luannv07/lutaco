package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_user_name",
                        columnNames = {"owner_user_id", "category_name"}
                )
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Category extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "category_name", nullable = false, length = 100)
    String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            foreignKey = @ForeignKey(name = "fk_categories_id_parent_id")
    )
    Category parent;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    Boolean isSystem = false;

    @Column(name = "owner_user_id")
    String ownerUserId;

    @Column(name = "category_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    CategoryType categoryType;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}