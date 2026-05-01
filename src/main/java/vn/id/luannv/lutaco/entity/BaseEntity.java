package vn.id.luannv.lutaco.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    Instant createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    Instant updatedDate;

    @Column(name = "created_by", updatable = false, length = 50)
    @CreatedBy
    String createdBy;

    @Column(name = "updated_by", length = 50)
    @LastModifiedBy
    String updatedBy;
}