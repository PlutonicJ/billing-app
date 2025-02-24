
package de.koedev.distribution.process;

import de.koedev.distribution.model.CustomerIdIbanCombination;
import de.koedev.distribution.model.repository.TransactionInfoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerBillingDistributionServiceTest {

    @Mock
    private CustomerAccountIntervalCreationService customerAccountIntervalCreationService;

    @Mock
    private ChargedBillingCycleCreationService chargedBillingCycleCreationService;

    @Mock
    private TransactionInfoRepository transactionInfoRepository;

    @InjectMocks
    private CustomerBillingDistributionService customerBillingDistributionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDistributeProcessingOfTransactionInfos() {
        // Fixes Zeitstempel
        LocalDateTime fixedNow = LocalDateTime.of(2025, 2, 28, 23, 59, 59);

        // Mock konfigurieren
        doNothing().when(customerAccountIntervalCreationService)
                .createCustomerAccountIntervals(eq(fixedNow), eq(Optional.empty()), eq("customerId"));
        doNothing().when(chargedBillingCycleCreationService)
                .createChargedBillingCycles(eq(fixedNow), anyString());

        Page<String> customerIdPage = new PageImpl<>(List.of("customerId"));
        when(transactionInfoRepository.findDistinctCustomerIdsWithNewTransactions(any(), any())).thenReturn(customerIdPage);
        Page<CustomerIdIbanCombination> customerIdIbanCombinationPage = new PageImpl<>(List.of(new CustomerIdIbanCombination("customerId", "iban")));
        when(transactionInfoRepository.findCustomerIdIbanCombinationsByCustomerId(anyString(), any(), any())).thenReturn(customerIdIbanCombinationPage);

        // Methode mit fixem Zeitstempel aufrufen
        customerBillingDistributionService.distributeProcessingOfTransactionInfos(fixedNow, "customerId", Optional.empty());

        // Argumente überprüfen
        verify(customerAccountIntervalCreationService)
                .createCustomerAccountIntervals(any(), eq(Optional.empty()), anyString());
        verify(chargedBillingCycleCreationService)
                .createChargedBillingCycles(eq(fixedNow), anyString());
    }
}
