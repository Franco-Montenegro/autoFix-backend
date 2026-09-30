package com.mingeso.backend.dto;

import java.math.BigDecimal;

/**
 * Celda de los reportes R2 y R4: count = lineas de reparacion; amount = suma de sus precios de lista.
 */
public record CountAmount(long count, BigDecimal amount) {

    public static final CountAmount EMPTY = new CountAmount(0, BigDecimal.ZERO);

    public CountAmount plus(CountAmount other) {
        return new CountAmount(count + other.count, amount.add(other.amount));
    }
}
