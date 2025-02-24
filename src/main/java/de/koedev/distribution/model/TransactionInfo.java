package de.koedev.distribution.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor(force = true)
@Table(name = "TRANSACTION_INFO")
public class TransactionInfo extends DomainObject {
    private static final long serialVersionUID = 1L;

    @Column(name = "CHARGEABLE_TX_ID", unique = true, nullable = false)
    private Long chargeableTxId;
    @Column(name = "CUSTOMER_ID", nullable = false)
    private String customerId;
    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;
    @Column(name = "IBAN", nullable = false)
    private String iban;

}