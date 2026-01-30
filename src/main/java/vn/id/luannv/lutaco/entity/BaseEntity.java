package vn.id.luannv.lutaco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseEntity implements Serializable {

    @CreatedBy
    @Column(name = "CREATED_BY")
    String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "UPDATED_BY")
    String updatedBy;

    @LastModifiedDate
    @Column(name = "UPDATED_DATE")
    LocalDateTime updatedDate;

}
