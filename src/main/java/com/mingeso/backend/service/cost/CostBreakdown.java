package com.mingeso.backend.service.cost;

import java.math.BigDecimal;

/**
 * Resultado del calculo: cada componente de la formula, en pesos enteros.
 * totalAmount = repairsSubtotal + recargos - descuentos + taxAmount.
 */
public record CostBreakdown(
        BigDecimal repairsSubtotal,
        BigDecimal mileageSurcharge,
        BigDecimal ageSurcharge,
        BigDecimal delaySurcharge,
        BigDecimal repairCountDiscount,
        BigDecimal dayDiscount,
        BigDecimal bonusDiscount,
        BigDecimal taxAmount,
        BigDecimal totalAmount
) {
}
