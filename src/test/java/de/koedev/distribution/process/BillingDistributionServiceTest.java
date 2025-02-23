
package de.koedev.distribution.process;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

class BillingDistributionServiceTest {

    @Mock
    private CustomerAccountIntervalCreationService customerAccountIntervalCreationService;

    @Mock
    private ChargedBillingCycleCreationService chargedBillingCycleCreationService;

    @InjectMocks
    private BillingDistributionService billingDistributionService;

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
                .createCustomerAccountIntervals(eq(fixedNow), eq(Optional.empty()));
        doNothing().when(chargedBillingCycleCreationService)
                .createChargedBillingCycles(eq(fixedNow));

        // Methode mit fixem Zeitstempel aufrufen
        billingDistributionService.distributeProcessingOfTransactionInfos(fixedNow);

        // Argumente überprüfen
        verify(customerAccountIntervalCreationService)
                .createCustomerAccountIntervals(eq(fixedNow), eq(Optional.empty()));
        verify(chargedBillingCycleCreationService)
                .createChargedBillingCycles(eq(fixedNow));
    }
}
