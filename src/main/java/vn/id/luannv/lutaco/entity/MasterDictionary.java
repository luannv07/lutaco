package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
        name = "master_dictionary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_master_dictionary_category_code",
                        columnNames = {"category", "code"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterDictionary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "category", length = 50, nullable = false)
    String category;

    @Column(name = "code", length = 50, nullable = false)
    String code;

    @Column(name = "value", length = 100, nullable = false)
    String value;

    @Column(name = "description", length = 255)
    String description;

    @Column(name = "is_active")
    Boolean isActive;
}
