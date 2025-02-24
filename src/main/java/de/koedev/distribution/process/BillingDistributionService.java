package de.koedev.distribution.process;

import de.koedev.distribution.model.repository.TransactionInfoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingDistributionService {

    private final CustomerAccountIntervalCreationService customerAccountIntervalCreationService;
    private final ChargedBillingCycleCreationService chargedBillingCycleCreationService;
    private final TransactionInfoRepository transactionInfoRepository;
    private final CustomerBillingDistributionService customerBillingDistributionService;

    public void distributeProcessingOfTransactionInfos(LocalDateTime now, Optional<LocalDate> billingDate) {
        Pageable pageable = PageRequest.of(0, 100);
        Page<String> page = transactionInfoRepository
                .findDistinctCustomerIdsWithNewTransactions(LocalDateTime.now().minusYears(1), pageable);

        while (page.hasContent()) {
            List<String> customerIds = page.getContent();

            customerIds.forEach(customerId -> {
                processCustomer(now, customerId, billingDate);
            });

            if (page.hasNext()) {
                pageable = page.nextPageable();
                page = transactionInfoRepository
                        .findDistinctCustomerIdsWithNewTransactions(LocalDateTime.now().minusYears(1), pageable);
            } else {
                break;
            }
        }
    }

    private void processCustomer(LocalDateTime now, String customerId, Optional<LocalDate> billingDate) {
        customerBillingDistributionService.distributeProcessingOfTransactionInfos(now, customerId, billingDate);
    }
}