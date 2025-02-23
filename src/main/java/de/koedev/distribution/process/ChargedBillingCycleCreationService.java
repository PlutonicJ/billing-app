package de.koedev.distribution.process;

import de.koedev.distribution.model.ChargedBillingCycle;
import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.CustomerAccountInterval_;
import de.koedev.distribution.model.ProcessState;
import de.koedev.distribution.model.repository.ChargedBillingCycleRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargedBillingCycleCreationService {

    private static final int PAGE_SIZE = 100; // Anzahl der Einträge pro Seite

    private final CustomerAccountIntervalRepository customerAccountIntervalRepository;
    private final ChargedBillingCycleRepository chargedBillingCycleRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void createChargedBillingCycles(@NonNull LocalDateTime now) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(CustomerAccountInterval_.CUSTOMER_ID));
        Page<String> customerIdPage;

        do {
            customerIdPage = customerAccountIntervalRepository.findDistinctCustomerIdsByCreatedDateTime(now, pageable);
            List<String> customerIds = customerIdPage.getContent();

            for (String customerId : customerIds) {
                processCustomerId(customerId, now);
            }

            pageable = customerIdPage.nextPageable();
        } while (customerIdPage.hasNext());
    }

    private void processCustomerId(String customerId, LocalDateTime now) {
        createChargedBillingCycle(customerId, now);
    }

    private void createChargedBillingCycle(String customerId, LocalDateTime now) {
        boolean exists = chargedBillingCycleRepository.existsByCustomerIdAndCreatedDateTime(customerId, now);

        if (!exists) {
            ChargedBillingCycle cycle = ChargedBillingCycle.builder()
                    .customerId(customerId)
                    .processState(ProcessState.PROCESSING_INITIATED)
                    .createdDateTime(now)
                    .build();
            chargedBillingCycleRepository.save(cycle);
        }
    }
}