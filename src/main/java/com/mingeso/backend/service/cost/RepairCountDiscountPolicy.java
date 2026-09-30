package com.mingeso.backend.service.cost;

import com.mingeso.backend.entity.EngineType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Descuento por numero de reparaciones en los ultimos 12 meses, segun el motor.
 * Se cuentan los ingresos previos del vehiculo, sin incluir el actual.
 */
@Component
public class RepairCountDiscountPolicy {

    /** Porcentajes por tramo: 1-2, 3-5, 6-9, 10 o mas. */
    private static int[] percentagesFor(EngineType engineType) {
        return switch (engineType) {
            case GASOLINE -> new int[]{5, 10, 15, 20};
            case DIESEL -> new int[]{7, 12, 17, 22};
            case HYBRID -> new int[]{10, 15, 20, 25};
            case ELECTRIC -> new int[]{8, 13, 18, 23};
        };
    }

    public BigDecimal rate(long previousRepairs, EngineType engineType) {
        if (previousRepairs <= 0) {
            return BigDecimal.ZERO;
        }
        int tier;
        if (previousRepairs <= 2) {
            tier = 0;
        } else if (previousRepairs <= 5) {
            tier = 1;
        } else if (previousRepairs <= 9) {
            tier = 2;
        } else {
            tier = 3;
        }
        return BigDecimal.valueOf(percentagesFor(engineType)[tier], 2);
    }
}
