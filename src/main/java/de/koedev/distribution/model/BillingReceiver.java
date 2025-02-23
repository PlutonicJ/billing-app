package de.koedev.distribution.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor(force = true)
@Table(name = "BILLING_RECEIVER")
public class BillingReceiver extends DomainObject {
    @Column(name = "CUSTOMER_ID", nullable = false)
    private String customerId;
    @Column(name = "BILLING_CYCLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;
}
