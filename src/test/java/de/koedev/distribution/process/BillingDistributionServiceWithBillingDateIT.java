package de.koedev.distribution.process;

import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.TransactionInfo;
import de.koedev.distribution.model.repository.ChargedBillingCycleRepository;
import de.koedev.distribution.model.repository.CustomerAccountIntervalRepository;
import de.koedev.distribution.model.repository.TransactionInfoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
public class BillingDistributionServiceWithBillingDateIT {

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

        // Fester Zeitpunkt für den Test
        LocalDateTime fixedNow = LocalDateTime.of(2025, 2, 28, 23, 59, 59);

        // Gruppe 1: 500 TransactionInfos, die ein abgeschlossenes Intervall haben
        LocalDateTime startDateTime1 = fixedNow.minusMonths(1);
        IntStream.range(0, 500).forEach(i -> {
            TransactionInfo transactionInfo = TransactionInfo.builder()
                    .chargeableTxId((long) i)
                    .customerId("customer-" + (i % 10))
                    .iban("DE89370400440532013000" + (i % 4))
                    .amount(BigDecimal.valueOf(100 + i))
                    .createdDateTime(startDateTime1.plusDays(i % 30))
                    .build();
            transactionInfoRepository.save(transactionInfo);
        });

        // Gruppe 2: 500 TransactionInfos, die noch nicht abgerechnet werden können
        LocalDateTime startDateTime2 = fixedNow.plusMonths(1);
        IntStream.range(0, 500).forEach(i -> {
            TransactionInfo transactionInfo = TransactionInfo.builder()
                    .chargeableTxId((long) 500 + i)
                    .customerId("customer-" + ((500 + i) % 3))
                    .iban("DE89370400440532013000" + (i % 5))
                    .amount(BigDecimal.valueOf(100 + i))
                    .createdDateTime(startDateTime2.plusDays(i % 30))
                    .build();
            transactionInfoRepository.save(transactionInfo);
        });

        // Überprüfen, ob alle TransactionInfos gespeichert wurden
        assertEquals(1000, transactionInfoRepository.count());
    }

    @Test
    @Transactional
    void testDistributeProcessingOfTransactionInfosWithBillingDate() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 2, 28, 23, 59, 59);
        Optional<LocalDate> billingDate = Optional.of(LocalDate.of(2025, 2, 28));

        // Starte die Verteilung und Verarbeitung der TransactionInfos mit billingDate
        billingDistributionService.distributeProcessingOfTransactionInfos(fixedNow, billingDate);

        // CustomerAccountIntervals und ChargedBillingCycles zählen
        long accountIntervalCount = customerAccountIntervalRepository.count();
        long chargedBillingCycleCount = chargedBillingCycleRepository.count();

        // Überprüfen, ob CustomerAccountIntervals und ChargedBillingCycles erzeugt wurden
        assertThat(accountIntervalCount).isGreaterThan(0);
        assertThat(chargedBillingCycleCount).isGreaterThan(0);

        // Überprüfen, ob nur für Gruppe 1 CustomerAccountIntervals erzeugt wurden
        assertEquals(20, accountIntervalCount);

        // Überprüfen, ob die Anzahl der ChargedBillingCycles korrekt ist
        assertThat(chargedBillingCycleCount).isGreaterThan(0);

        Pageable pageable = PageRequest.of(0, 1000);
        LocalDateTime intervalCreatedDateTime = customerAccountIntervalRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(CustomerAccountInterval::getCreatedDateTime)
                .findFirst()
                .orElseThrow();

        boolean allEqual = customerAccountIntervalRepository.findAll(pageable)
                .getContent()
                .stream()
                .allMatch(interval -> interval.getCreatedDateTime().isEqual(intervalCreatedDateTime));
        assertThat(allEqual).isTrue();

        // Debug-Ausgaben zur Kontrolle
        System.out.println("Erzeugte CustomerAccountIntervals mit billingDate: " + accountIntervalCount);
        System.out.println("Erzeugte ChargedBillingCycles mit billingDate: " + chargedBillingCycleCount);
    }

    @Test
    @Transactional
    void testDistributeProcessingOfTransactionInfosWithBillingDateEarlier() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 2, 28, 23, 59, 59);
        Optional<LocalDate> billingDate = Optional.of(LocalDate.of(2025, 2, 23));

        // Starte die Verteilung und Verarbeitung der TransactionInfos mit billingDate
        billingDistributionService.distributeProcessingOfTransactionInfos(fixedNow, billingDate);

        // CustomerAccountIntervals und ChargedBillingCycles zählen
        long accountIntervalCount = customerAccountIntervalRepository.count();
        long chargedBillingCycleCount = chargedBillingCycleRepository.count();

        // Überprüfen, ob CustomerAccountIntervals und ChargedBillingCycles erzeugt wurden
        assertThat(accountIntervalCount).isGreaterThan(0);
        assertThat(chargedBillingCycleCount).isGreaterThan(0);

        // Überprüfen, ob nur für Gruppe 1 CustomerAccountIntervals erzeugt wurden
        assertEquals(20, accountIntervalCount);

        // Überprüfen, ob die Anzahl der ChargedBillingCycles korrekt ist
        assertThat(chargedBillingCycleCount).isGreaterThan(0);

        Pageable pageable = PageRequest.of(0, 1000);
        LocalDateTime intervalCreatedDateTime = customerAccountIntervalRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(CustomerAccountInterval::getCreatedDateTime)
                .findFirst()
                .orElseThrow();

        boolean allEqual = customerAccountIntervalRepository.findAll(pageable)
                .getContent()
                .stream()
                .allMatch(interval -> interval.getCreatedDateTime().isEqual(intervalCreatedDateTime));
        assertThat(allEqual).isTrue();

        // 🆕 Ermitteln der TransactionInfos, die zu den CustomerAccountIntervals gehören
        customerAccountIntervalRepository.findAll(pageable).getContent().forEach(interval -> {
            System.out.println("CustomerAccountInterval: " + interval.getCustomerId() + " / " + interval.getIban());

            // 🧠 TransactionInfos für jedes CustomerAccountInterval ermitteln
            List<TransactionInfo> transactionInfos = transactionInfoRepository.findByCustomerIdAndIbanAndCreatedDateTimeBetween(
                    interval.getCustomerId(),
                    interval.getIban(),
                    interval.getIntervalStart().atStartOfDay(),
                    interval.getIntervalEnd().plusDays(1).atStartOfDay() // Bis einschließlich Intervall-Ende
            );

            // Debug-Ausgabe der zugehörigen TransactionInfos
            transactionInfos.forEach(transactionInfo -> {
                System.out.println("TransactionInfo ID: " + transactionInfo.getChargeableTxId() +
                        " | Created: " + transactionInfo.getCreatedDateTime());
            });

            // Überprüfen, ob nur erwartete TransactionInfos gefunden werden
            boolean allTransactionInfosValid = transactionInfos.stream()
                    .allMatch(ti -> !ti.getCreatedDateTime().isAfter(interval.getIntervalEnd().atTime(23, 59, 59))
                            && !ti.getCreatedDateTime().isBefore(interval.getIntervalStart().atStartOfDay()));

            assertThat(allTransactionInfosValid).isTrue();
        });

        long transactionInfosCount = customerAccountIntervalRepository.findAll(pageable).getContent().stream()
                .mapToInt(cai -> transactionInfoRepository.findByCustomerIdAndIbanAndCreatedDateTimeBetween(
                        cai.getCustomerId(),
                        cai.getIban(),
                        cai.getIntervalStart().atStartOfDay(),
                        cai.getIntervalEnd().plusDays(1).atStartOfDay() // Bis einschließlich Intervall-Ende
                ).size()).sum();
        assertEquals(384, transactionInfosCount);

        // Debug-Ausgaben zur Kontrolle
        System.out.println("Erzeugte CustomerAccountIntervals mit billingDate: " + accountIntervalCount);
        System.out.println("Erzeugte ChargedBillingCycles mit billingDate: " + chargedBillingCycleCount);
    }
}