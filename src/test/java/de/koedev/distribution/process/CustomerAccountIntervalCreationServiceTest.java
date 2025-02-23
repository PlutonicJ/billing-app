
package de.koedev.distribution.process;

import de.koedev.distribution.model.CustomerIdIbanCombination;
import de.koedev.distribution.model.repository.BillingReceiverAccountRepository;
import de.koedev.distribution.model.repository.BillingReceiverRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
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
import org.springframework.data.domain.Pageable;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerAccountIntervalCreationServiceTest {

    @Mock
    private TransactionInfoRepository transactionInfoRepository;

    @Mock
    private CustomerAccountIntervalRepository customerAccountIntervalRepository;

    @Mock
    private BillingReceiverRepository billingReceiverRepository;

    @Mock
    private BillingReceiverAccountRepository billingReceiverAccountRepository;

    @Mock
    private BillingIntervalService billingIntervalService;

    @InjectMocks
    private CustomerAccountIntervalCreationService customerAccountIntervalCreationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCustomerAccountIntervals() {
        // Testdaten vorbereiten
        CustomerIdIbanCombination combination = new CustomerIdIbanCombination("customer1", "iban1");
        List<CustomerIdIbanCombination> combinations = List.of(combination);

        // Page mit den Testdaten erstellen
        Page<CustomerIdIbanCombination> page = new PageImpl<>(combinations);

        // Mock für findCustomerIdIbanCombinations
        when(transactionInfoRepository.findCustomerIdIbanCombinations(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        // Test ausführen
        customerAccountIntervalCreationService.createCustomerAccountIntervals(LocalDateTime.now(), Optional.empty());

        // Überprüfen, ob die Methode aufgerufen wurde
        verify(transactionInfoRepository).findCustomerIdIbanCombinations(any(LocalDateTime.class), any(Pageable.class));
    }
}
