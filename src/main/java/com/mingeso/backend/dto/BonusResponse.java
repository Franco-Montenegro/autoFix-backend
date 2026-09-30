package com.mingeso.backend.dto;

import com.mingeso.backend.entity.Bonus;

import java.math.BigDecimal;

/**
 * Cupo de bonos con su uso: used = ingresos que lo tienen asignado; available = quantity - used.
 */
public record BonusResponse(
        Long id,
        String brand,
        Integer year,
        Integer month,
        Integer quantity,
        BigDecimal amount,
        long used,
        long available
) {
    public static BonusResponse from(Bonus bonus, long used) {
        return new BonusResponse(
                bonus.getId(),
                bonus.getBrand(),
                bonus.getPeriodYear(),
                bonus.getPeriodMonth(),
                bonus.getQuantity(),
                bonus.getAmount(),
                used,
                Math.max(0, bonus.getQuantity() - used)
        );
    }
}
