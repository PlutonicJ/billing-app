package de.koedev.distribution.process;

import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.TransactionInfo;
import de.koedev.distribution.model.repository.ChargedBillingCycleRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
import de.koedev.distribution.model.repository.TransactionInfoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor
public class BillingDistributionServiceNoCustomerAccountIntervalCreatedIT {

    @Autowired
    private BillingDistributionService billingDistributionService;

    @Autowired
    private TransactionInfoRepository transactionInfoRepository;

    @Autowired
    private CustomerAccountIntervalRepository customerAccountIntervalRepository;

    @Autowired
    private ChargedBillingCycleRepository chargedBillingCycleRepository;

    @BeforeEach
    void setUp() {
        // Vor jedem Test Datenbank leeren
        transactionInfoRepository.deleteAll();
        customerAccountIntervalRepository.deleteAll();
        chargedBillingCycleRepository.deleteAll();
    }

    @Test
    @Transactional
    void testNoCustomerAccountIntervalCreated() {
        // 1. TransactionInfos erzeugen, die außerhalb des Abrechnungsintervalls liegen
        LocalDateTime fixedNow = LocalDateTime.of(2025, 2, 28, 23, 59, 59);
        LocalDateTime startDateTime = fixedNow.plusMonths(2); // Liegt in der Zukunft

        IntStream.range(0, 100).forEach(i -> {
            TransactionInfo transactionInfo = TransactionInfo.builder()
                    .chargeableTxId((long) i)
                    .customerId("no-interval-customer")
                    .iban("DE893704004405320130000")
                    .amount(BigDecimal.valueOf(100 + i))
                    .createdDateTime(startDateTime.plusDays(i % 30))
                    .build();
            transactionInfoRepository.save(transactionInfo);
        });

        // Sicherstellen, dass 100 TransactionInfos gespeichert wurden
        long transactionInfoCount = transactionInfoRepository.count();
        assertEquals(100, transactionInfoCount);

        // 2. Verarbeitung starten
        billingDistributionService.distributeProcessingOfTransactionInfos(fixedNow);

        // 3. Überprüfen, dass KEINE CustomerAccountIntervals erzeugt wurden
        long accountIntervalCount = customerAccountIntervalRepository.count();
        assertEquals(0, accountIntervalCount);

        // 4. Überprüfen, dass KEINE ChargedBillingCycles erzeugt wurden
        long chargedBillingCycleCount = chargedBillingCycleRepository.count();
        assertEquals(0, chargedBillingCycleCount);

        // Debug-Ausgaben zur Kontrolle
        System.out.println("TransactionInfos: " + transactionInfoCount);
        System.out.println("Erzeugte CustomerAccountIntervals: " + accountIntervalCount);
        System.out.println("Erzeugte ChargedBillingCycles: " + chargedBillingCycleCount);
    }
}