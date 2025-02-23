package de.koedev.distribution.model.repository;

import de.koedev.distribution.model.BillingReceiverAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingReceiverAccountRepository extends JpaRepository<BillingReceiverAccount, Long> {
    Optional<BillingReceiverAccount> findByCustomerIdAndIban(String customerId, String iban);

    List<BillingReceiverAccount> findByCustomerId(String customerId);
}
