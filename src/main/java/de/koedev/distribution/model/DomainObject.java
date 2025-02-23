package de.koedev.distribution.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

@Data
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class DomainObject implements Serializable {
    private static final long serialVersionUID = -1L;
    @Id
    @SequenceGenerator(name = "ID_GEN", sequenceName = "S_BILLING_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID_GEN")
    @Column(name = "PK_ID")
    private Long id;
    @Version
    private int version;
    @Getter
    @CreatedDate
    @Builder.Default
    @Column(name = "CREATED_DATE_TIME", nullable = false, updatable = false)
    private LocalDateTime createdDateTime = LocalDateTime.now();
}
