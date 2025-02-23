package de.koedev.distribution.process;

import de.koedev.distribution.model.BillingCycle;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BillingIntervalService {

    /**
     * Berechnet das Intervall auf Basis von today, Optional<LocalDate> billingDate und cycle.
     * - Wenn billingDate gesetzt ist, wird dieses als Intervall-Ende verwendet.
     * - Wenn billingDate nicht gesetzt ist, wird das Intervall-Ende auf Basis von cycle ermittelt.
     *
     * @param today       Aktueller Zeitpunkt als Basis
     * @param billingDate Optionales Intervall-Ende
     * @param cycle       BillingCycle (MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL)
     * @return BillingInterval mit Start, Ende und Period
     */
    public BillingInterval calculateInterval(@NonNull LocalDate today, Optional<LocalDate> billingDate, @NonNull BillingCycle cycle) {
        LocalDate end = billingDate.orElseGet(() -> calculateEnd(today, cycle));
        LocalDate start = calculateStart(today, cycle, end);
        Period period = getPeriod(cycle);

        return new BillingInterval(start, end, period);
    }

    /**
     * Berechnet den Start des Intervalls basierend auf today und cycle.
     * Das Jahr des Intervall-Starts wird an das Jahr von today angepasst.
     * Ausnahmen:
     * - ANNUAL: Startet immer am 1. Januar des Jahres, das zu endDate passt.
     *
     * @param today    Aktueller Zeitpunkt
     * @param cycle    BillingCycle (MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL)
     * @param endDate  Ende des Intervalls
     * @return Intervall-Start als LocalDate
     */
    private LocalDate calculateStart(LocalDate today, BillingCycle cycle, LocalDate endDate) {
        return switch (cycle) {
            case MONTHLY -> LocalDate.of(today.getYear(), today.getMonth(), 1);
            case QUARTERLY -> {
                int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
                int startMonth = (currentQuarter - 1) * 3 + 1;
                yield LocalDate.of(today.getYear(), startMonth, 1);
            }
            case SEMI_ANNUAL -> today.getMonthValue() <= 6
                    ? LocalDate.of(today.getYear(), 1, 1)
                    : LocalDate.of(today.getYear(), 7, 1);
            case ANNUAL -> LocalDate.of(endDate.getYear(), 1, 1);
        };
    }

    /**
     * Berechnet das Ende des Intervalls basierend auf today und cycle.
     * @param today Aktueller Zeitpunkt
     * @param cycle BillingCycle (MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL)
     * @return Intervall-Ende als LocalDate
     */
    private LocalDate calculateEnd(LocalDate today, BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> today.withDayOfMonth(today.lengthOfMonth());
            case QUARTERLY -> {
                int quarter = (today.getMonthValue() - 1) / 3 + 1;
                yield LocalDate.of(today.getYear(), quarter * 3, 1)
                        .withDayOfMonth(LocalDate.of(today.getYear(), quarter * 3, 1).lengthOfMonth());
            }
            case SEMI_ANNUAL -> today.getMonthValue() <= 6
                    ? LocalDate.of(today.getYear(), 6, 30)
                    : LocalDate.of(today.getYear(), 12, 31);
            case ANNUAL -> LocalDate.of(today.getYear(), 12, 31);
        };
    }

    private Period getPeriod(BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> Period.ofMonths(1);
            case QUARTERLY -> Period.ofMonths(3);
            case SEMI_ANNUAL -> Period.ofMonths(6);
            case ANNUAL -> Period.ofYears(1);
        };
    }
}