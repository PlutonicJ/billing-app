package de.koedev.distribution.process;

import de.koedev.distribution.model.BillingCycle;
import de.koedev.distribution.model.BillingReceiver;
import de.koedev.distribution.model.BillingReceiverAccount;
import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.CustomerIdIbanCombination;
import de.koedev.distribution.model.TransactionInfo_;
import de.koedev.distribution.model.repository.BillingReceiverAccountRepository;
import de.koedev.distribution.model.repository.BillingReceiverRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
import de.koedev.distribution.model.repository.TransactionInfoRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerAccountIntervalCreationService {

    private final TransactionInfoRepository transactionInfoRepository;
    private final BillingReceiverRepository billingReceiverRepository;
    private final BillingReceiverAccountRepository billingReceiverAccountRepository;
    private final CustomerAccountIntervalRepository customerAccountIntervalRepository;
    private final BillingIntervalService intervalService;

    private static final int PAGE_SIZE = 100;

    @Transactional(propagation = Propagation.MANDATORY)
    public void createCustomerAccountIntervals(@NonNull LocalDateTime now, Optional<LocalDate> billingDate) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(TransactionInfo_.CUSTOMER_ID, TransactionInfo_.IBAN));
        Page<CustomerIdIbanCombination> page;

        do {
            page = transactionInfoRepository.findCustomerIdIbanCombinations(now, pageable);
            processCombinations(page.getContent(), now, billingDate);
            pageable = page.nextPageable();
        } while (page.hasNext());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createCustomerAccountIntervals(@NonNull LocalDateTime now, Optional<LocalDate> billingDate, @NonNull String customerId) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(TransactionInfo_.CUSTOMER_ID, TransactionInfo_.IBAN));
        Page<CustomerIdIbanCombination> page;

        do {
            page = transactionInfoRepository.findCustomerIdIbanCombinations(customerId, now, pageable);
            processCombinations(page.getContent(), now, billingDate);
            pageable = page.nextPageable();
        } while (page.hasNext());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createCustomerAccountIntervals(@NonNull LocalDateTime now, Optional<LocalDate> billingDate, @NonNull String customerId, @NonNull String iban) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(TransactionInfo_.CUSTOMER_ID, TransactionInfo_.IBAN));
        Page<CustomerIdIbanCombination> page;

        do {
            page = transactionInfoRepository.findCustomerIdIbanCombinations(customerId, iban, now, pageable);
            processCombinations(page.getContent(), now, billingDate);
            pageable = page.nextPageable();
        } while (page.hasNext());
    }

    private void processCombinations(List<CustomerIdIbanCombination> combinations, @NonNull LocalDateTime now, Optional<LocalDate> billingDate) {
        String currentCustomerId = null;
        Optional<BillingReceiver> currentReceiver = Optional.empty();
        Map<String, BillingReceiverAccount> currentReceiverAccounts = null;

        for (CustomerIdIbanCombination combination : combinations) {
            if (!combination.customerId().equals(currentCustomerId)) {
                currentCustomerId = combination.customerId();
                currentReceiver = billingReceiverRepository.findByCustomerId(currentCustomerId);

                currentReceiverAccounts = billingReceiverAccountRepository.findByCustomerId(currentCustomerId)
                        .stream().collect(Collectors.toMap(BillingReceiverAccount::getIban, account -> account));
            }

            processCombination(combination, currentReceiver, currentReceiverAccounts, now, billingDate);
        }
    }

    private void processCombination(CustomerIdIbanCombination combination,
                                    Optional<BillingReceiver> receiver,
                                    Map<String, BillingReceiverAccount> receiverAccounts,
                                    LocalDateTime now,
                                    Optional<LocalDate> billingDate) {

        BillingCycle billingCycle = Optional.ofNullable(receiverAccounts.get(combination.iban()))
                .map(BillingReceiverAccount::getBillingCycle)
                .or(() -> receiver.map(BillingReceiver::getBillingCycle))
                .orElse(BillingCycle.MONTHLY);

        Optional<BillingInterval> interval = Optional.ofNullable(intervalService.calculateInterval(now.toLocalDate(), billingDate, billingCycle));
        if (shouldCreateCustomerAccountInterval(now, interval)) {
            Optional<CustomerAccountInterval> customerAccountInterval = createCustomerAccountInterval(combination.customerId(), combination.iban(), interval, now);
            customerAccountInterval.ifPresent(customerAccountIntervalRepository::save);
        }
    }

    private boolean shouldCreateCustomerAccountInterval(LocalDateTime now, Optional<BillingInterval> intervalOptional) {
        if (intervalOptional.isEmpty()) {
            return false;
        }
        BillingInterval interval = intervalOptional.get();
        return now.toLocalDate().isAfter(interval.end()) || now.toLocalDate().isEqual(interval.end());
    }

    private Optional<CustomerAccountInterval> createCustomerAccountInterval(String customerId, String iban, Optional<BillingInterval> billingIntervalOptional, LocalDateTime now) {
        if (billingIntervalOptional.isEmpty()) {
            return Optional.empty();
        }
        BillingInterval billingInterval = billingIntervalOptional.get();
        return Optional.of(CustomerAccountInterval.builder()
                .customerId(customerId)
                .iban(iban)
                .intervalStart(billingInterval.start())
                .intervalEnd(billingInterval.end())
                .createdDateTime(now)
                .build());
    }
}