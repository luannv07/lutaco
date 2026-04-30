package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    Instant createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    Instant updatedDate;

    @Column(name = "created_by", updatable = false, length = 50)
    String createdBy;

    @Column(name = "updated_by", length = 50)
    String updatedBy;
}