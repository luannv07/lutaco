package vn.id.luannv.lutaco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "master_dictionary", uniqueConstraints = {
        @UniqueConstraint(name = "uk_master_dictionary_group_code", columnNames = {"dict_group", "code"})
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterDictionary extends BaseEntity {

    @Column(name = "dict_group", nullable = false, length = 100)
    String dictGroup;

    @Column(name = "code", nullable = false, length = 100)
    String code;

    @Column(name = "value_vi", nullable = false, length = 255)
    String valueVi;

    @Column(name = "value_en", nullable = false, length = 255)
    String valueEn;

    @Column(name = "active_flg")
    Boolean activeFlg = Boolean.TRUE;

    @Column(name = "display_order")
    Integer displayOrder;
}
