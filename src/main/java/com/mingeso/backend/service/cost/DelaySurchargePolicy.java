package com.mingeso.backend.service.cost;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Recargo del 5% por cada dia calendario entre la salida de la reparacion y el retiro.
 */
@Component
public class DelaySurchargePolicy {

    private static final BigDecimal RATE_PER_DAY = BigDecimal.valueOf(5, 2);

    public long delayDays(LocalDateTime readyDateTime, LocalDateTime pickupDateTime) {
        return Math.max(0, ChronoUnit.DAYS.between(readyDateTime.toLocalDate(), pickupDateTime.toLocalDate()));
    }

    public BigDecimal rate(LocalDateTime readyDateTime, LocalDateTime pickupDateTime) {
        return RATE_PER_DAY.multiply(BigDecimal.valueOf(delayDays(readyDateTime, pickupDateTime)));
    }
}
