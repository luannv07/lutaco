package vn.id.luannv.lutaco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.id.luannv.lutaco.enumerate.WalletStatus;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "WALLETS")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", updatable = false, nullable = false)
    String id;

    @Column(name = "WALLET_name", nullable = false)
    String walletName;

    @Column(name = "INITIAL_BALANCE", nullable = false)
    Long initialBalance;

    @Column(name = "CURRENT_BALANCE", nullable = false)
    Long currentBalance;

    @Column(name = "DESCRIPTION")
    String description;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    WalletStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    @JsonIgnore
    User user;
}
