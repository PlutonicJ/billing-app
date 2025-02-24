package de.koedev.distribution.process;

import de.koedev.distribution.model.ChargedBillingCycle;
import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.ProcessState;
import de.koedev.distribution.model.repository.ChargedBillingCycleRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChargedBillingCycleCreationService {

    private final CustomerAccountIntervalRepository customerAccountIntervalRepository;
    private final ChargedBillingCycleRepository chargedBillingCycleRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void createChargedBillingCycles(LocalDateTime now, String customerId) {
        long numberOfIntervals = customerAccountIntervalRepository
                .findByCustomerIdAndCreatedDateTime(customerId, now, Pageable.ofSize(1)).getTotalElements();

        if (numberOfIntervals > 0) {
            ChargedBillingCycle chargedBillingCycle = createChargedBillingCycle(customerId, now);
            chargedBillingCycleRepository.save(chargedBillingCycle);
        }
    }

    private ChargedBillingCycle createChargedBillingCycle(String customerId, LocalDateTime now) {
        return ChargedBillingCycle.builder()
                .customerId(customerId)
                .processState(ProcessState.PROCESSING_INITIATED)
                .createdDateTime(now)
                .build();
    }
}