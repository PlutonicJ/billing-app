package de.koedev.distribution.model.repository;

import de.koedev.distribution.model.ChargedBillingCycle;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargedBillingCycleRepository extends JpaRepository<ChargedBillingCycle, Long> {

    boolean existsByCustomerIdAndCreatedDateTime(String customerId, LocalDateTime createdDateTime);
}