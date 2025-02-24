package de.koedev.distribution.process;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerBillingDistributionService {

    private final CustomerAccountIntervalCreationService customerAccountIntervalCreationService;
    private final ChargedBillingCycleCreationService chargedBillingCycleCreationService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void distributeProcessingOfTransactionInfos(LocalDateTime now, String customerId, Optional<LocalDate> billingDate) {
        processCustomer(now, customerId, billingDate);
    }

    private void processCustomer(LocalDateTime now, String customerId, Optional<LocalDate> billingDate) {
        customerAccountIntervalCreationService.createCustomerAccountIntervals(now, billingDate, customerId);
        chargedBillingCycleCreationService.createChargedBillingCycles(now, customerId);
    }
}