
package de.koedev.distribution.process;

import de.koedev.distribution.model.ChargedBillingCycle;
import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.repository.ChargedBillingCycleRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChargedBillingCycleCreationServiceTest {

    @Mock
    private CustomerAccountIntervalRepository customerAccountIntervalRepository;

    @Mock
    private ChargedBillingCycleRepository chargedBillingCycleRepository;

    @InjectMocks
    private ChargedBillingCycleCreationService chargedBillingCycleCreationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateChargedBillingCycles() {
        LocalDateTime now = LocalDateTime.now();
        CustomerAccountInterval interval = CustomerAccountInterval.builder()
                .customerId("exampleCustomerId")
                .createdDateTime(now)
                .intervalEnd(now.toLocalDate())
                .build();

        Page<CustomerAccountInterval> intervalPage = new PageImpl<>(List.of(interval));

        when(customerAccountIntervalRepository.findDistinctCustomerIdsByCreatedDateTime(any(), any()))
                .thenReturn(new PageImpl<>(List.of("exampleCustomerId")));
        when(customerAccountIntervalRepository.findByCustomerIdAndCreatedDateTime(anyString(), any(), any()))
                .thenReturn(intervalPage);
        when(chargedBillingCycleRepository.existsByCustomerIdAndCreatedDateTime("exampleCustomerId", now))
                .thenReturn(false);

        chargedBillingCycleCreationService.createChargedBillingCycles(now, "exampleCustomerId");

        verify(chargedBillingCycleRepository, times(1)).save(any(ChargedBillingCycle.class));
    }
}
