package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
import vn.id.luannv.lutaco.enumerate.CategoryType;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_categories_parent_code",
                        columnNames = {"parent_id", "category_code"}
                )
        },
        indexes = {
                @Index(name = "idx_categories_type", columnList = "category_type"),
                @Index(name = "idx_categories_parent", columnList = "parent_id")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category extends BaseEntity {

    @Column(name = "category_code", nullable = false, length = 100)
    String categoryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 10)
    CategoryType categoryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    Set<Category> children = new HashSet<>();

    @Column(name = "active_flg", nullable = false)
    boolean activeFlg = true;
}