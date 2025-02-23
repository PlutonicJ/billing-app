package de.koedev.distribution.model.repository;

import de.koedev.distribution.model.BillingReceiver;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingReceiverRepository extends JpaRepository<BillingReceiver, Long> {
    Optional<BillingReceiver> findByCustomerId(String customerId);
}
