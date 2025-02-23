
package de.koedev.distribution.process;

import de.koedev.distribution.model.BillingCycle;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingIntervalServiceTest {
    @Test
    void testCalculateIntervalMonthly() {
        BillingIntervalService service = new BillingIntervalService();
        LocalDate today = LocalDate.of(2023, 1, 15); // 15. Januar 2023
        BillingInterval interval = service.calculateInterval(today, Optional.empty(), BillingCycle.MONTHLY);
        assertEquals(LocalDate.of(2023, 1, 1), interval.start());
        assertEquals(LocalDate.of(2023, 1, 31), interval.end());
    }

    @Test
    void testCalculateIntervalQuarterly() {
        BillingIntervalService service = new BillingIntervalService();
        LocalDate today = LocalDate.of(2023, 1, 15); // 15. Januar 2023
        BillingInterval interval = service.calculateInterval(today, Optional.empty(), BillingCycle.QUARTERLY);
        assertEquals(LocalDate.of(2023, 1, 1), interval.start());
        assertEquals(LocalDate.of(2023, 3, 31), interval.end());
    }
}
