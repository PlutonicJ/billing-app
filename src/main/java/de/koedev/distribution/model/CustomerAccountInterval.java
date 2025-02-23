package de.koedev.distribution.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor(force = true)
@Table(name = "CUSTOMER_ACCOUNT_INTERVAL", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_CAI_CCI", columnNames = {"CREATED_DATE_TIME", "CUSTOMER_ID", "IBAN"})
})
public class CustomerAccountInterval extends DomainObject {
    private static final long serialVersionUID = 1L;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private String customerId;

    @Column(name = "IBAN", nullable = false)
    private String iban;

    @Column(name = "INTERVAL_START", nullable = false)
    private LocalDate intervalStart;

    @Column(name = "INTERVAL_END", nullable = false)
    private LocalDate intervalEnd;
}

