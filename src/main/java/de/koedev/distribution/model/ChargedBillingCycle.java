package de.koedev.distribution.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor(force = true)
@Table(name = "CHARGED_BILLING_CYCLE", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_CBC_CC", columnNames = {"CREATED_DATE_TIME", "CUSTOMER_ID"})
})
public class ChargedBillingCycle extends DomainObject {
    private static final long serialVersionUID = 1L;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROCESS_STATE", nullable = false)
    private ProcessState processState;
}

