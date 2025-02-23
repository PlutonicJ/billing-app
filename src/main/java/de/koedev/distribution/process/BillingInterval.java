package de.koedev.distribution.process;

import java.time.LocalDate;
import java.time.Period;
import lombok.NonNull;

public record BillingInterval(@NonNull LocalDate start, @NonNull LocalDate end, @NonNull Period period) {

    public BillingInterval {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
    }

    @Override
    public String toString() {
        return "BillingInterval[start=%s, end=%s, period=%s]".formatted(start, end, period);
    }
}