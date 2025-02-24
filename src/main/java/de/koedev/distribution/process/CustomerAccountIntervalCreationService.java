package de.koedev.distribution.process;

import de.koedev.distribution.model.BillingCycle;
import de.koedev.distribution.model.BillingReceiver;
import de.koedev.distribution.model.BillingReceiverAccount;
import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.CustomerIdIbanCombination;
import de.koedev.distribution.model.repository.BillingReceiverAccountRepository;
import de.koedev.distribution.model.repository.BillingReceiverRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
import de.koedev.distribution.model.repository.TransactionInfoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerAccountIntervalCreationService {

    private final TransactionInfoRepository transactionInfoRepository;
    private final CustomerAccountIntervalRepository customerAccountIntervalRepository;
    private final BillingIntervalService intervalService;
    private final BillingReceiverRepository billingReceiverRepository;
    private final BillingReceiverAccountRepository billingReceiverAccountRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void createCustomerAccountIntervals(LocalDateTime now, Optional<LocalDate> billingDate, String customerId) {
        Pageable pageable = PageRequest.of(0, 100);
        Page<CustomerIdIbanCombination> page = transactionInfoRepository
                .findCustomerIdIbanCombinationsByCustomerId(customerId, now, pageable);
        Optional<BillingReceiver> billingReceiver = billingReceiverRepository.findByCustomerId(customerId);
        List<BillingReceiverAccount> billingReceiverAccounts = billingReceiverAccountRepository.findByCustomerId(customerId);

        while (page.hasContent()) {
            List<CustomerIdIbanCombination> combinations = page.getContent();
            combinations.forEach(c -> {
                BillingCycle billingCycle = billingReceiverAccounts.stream()
                        .map(BillingReceiverAccount::getBillingCycle)
                        .findFirst()  // Nimmt den ersten gefundenen BillingCycle
                        .or(() -> billingReceiver.map(BillingReceiver::getBillingCycle))
                        .orElse(BillingCycle.MONTHLY);
                processCombination(c, now, billingDate, billingCycle);
            });

            if (page.hasNext()) {
                pageable = page.nextPageable();
                page = transactionInfoRepository.findCustomerIdIbanCombinationsByCustomerId(customerId, now, pageable);
            } else {
                break;
            }
        }
    }

    private void processCombination(CustomerIdIbanCombination combination,
                                    LocalDateTime now,
                                    Optional<LocalDate> billingDate,
                                    BillingCycle billingCycle) {

        BillingInterval interval = intervalService.calculateInterval(now.toLocalDate(), billingDate, billingCycle);

        if (shouldCreateCustomerAccountInterval(now, interval) && !isOverlapping(combination.customerId(), combination.iban(), interval.start(), interval.end())) {
            CustomerAccountInterval customerAccountInterval = createCustomerAccountInterval(combination, interval, now);
            customerAccountIntervalRepository.save(customerAccountInterval);
        }
    }

    private boolean shouldCreateCustomerAccountInterval(LocalDateTime now, @NonNull BillingInterval interval) {
        return now.toLocalDate().isAfter(interval.end()) || now.toLocalDate().isEqual(interval.end());
    }

    private boolean isOverlapping(String customerId, String iban, LocalDate intervalStart, LocalDate intervalEnd) {
        int count = customerAccountIntervalRepository.countOverlappingIntervals(customerId, iban, intervalStart, intervalEnd);
        return count > 0;
    }

    private CustomerAccountInterval createCustomerAccountInterval(CustomerIdIbanCombination combination, BillingInterval billingInterval, LocalDateTime now) {
        return CustomerAccountInterval.builder()
                .customerId(combination.customerId())
                .iban(combination.iban())
                .intervalStart(billingInterval.start())
                .intervalEnd(billingInterval.end())
                .createdDateTime(now)
                .build();
    }
}