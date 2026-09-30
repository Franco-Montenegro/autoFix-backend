package com.mingeso.backend.service.cost;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * Descuento del 10% si el vehiculo ingresa lunes o jueves entre 09:00 y 12:00 (ambos incluidos).
 */
@Component
public class AttentionDayDiscountPolicy {

    private static final Set<DayOfWeek> DAYS = Set.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY);
    private static final LocalTime FROM = LocalTime.of(9, 0);
    private static final LocalTime TO = LocalTime.of(12, 0);
    private static final BigDecimal RATE = BigDecimal.valueOf(10, 2);

    public BigDecimal rate(LocalDateTime entryDateTime) {
        LocalTime time = entryDateTime.toLocalTime();
        boolean applies = DAYS.contains(entryDateTime.getDayOfWeek())
                && !time.isBefore(FROM)
                && !time.isAfter(TO);
        return applies ? RATE : BigDecimal.ZERO;
    }
}
