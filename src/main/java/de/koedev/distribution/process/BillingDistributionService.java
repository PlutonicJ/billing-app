package de.koedev.distribution.process;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingDistributionService {
    private final CustomerAccountIntervalCreationService customerAccountIntervalCreationService;
    private final ChargedBillingCycleCreationService chargedBillingCycleCreationService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void distributeProcessingOfTransactionInfos(LocalDateTime now) {
        customerAccountIntervalCreationService.createCustomerAccountIntervals(now, Optional.empty());
        chargedBillingCycleCreationService.createChargedBillingCycles(now);
    }
}
