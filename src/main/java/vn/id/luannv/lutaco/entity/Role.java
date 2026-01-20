package vn.id.luannv.lutaco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ROLES")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    Integer id;
    @Column(name = "NAME", unique = true, nullable = false)
    String name;
    @Column(name = "DESCRIPTION")
    String description;
    @OneToMany(mappedBy = "role")
    @JsonIgnore
    List<User> users;
}
