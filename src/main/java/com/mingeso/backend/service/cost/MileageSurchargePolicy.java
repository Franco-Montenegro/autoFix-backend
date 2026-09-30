package com.mingeso.backend.service.cost;

import com.mingeso.backend.entity.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Recargo por kilometraje segun el tipo de vehiculo.
 * Tramos: 0-5.000, 5.001-12.000, 12.001-25.000, 25.001-40.000, desde 40.001.
 */
@Component
public class MileageSurchargePolicy {

    private static int[] percentagesFor(VehicleType vehicleType) {
        return switch (vehicleType) {
            case SEDAN, HATCHBACK -> new int[]{0, 3, 7, 12, 20};
            case SUV, PICKUP, VAN -> new int[]{0, 5, 9, 12, 20};
        };
    }

    public BigDecimal rate(int mileage, VehicleType vehicleType) {
        int tier;
        if (mileage <= 5_000) {
            tier = 0;
        } else if (mileage <= 12_000) {
            tier = 1;
        } else if (mileage <= 25_000) {
            tier = 2;
        } else if (mileage <= 40_000) {
            tier = 3;
        } else {
            tier = 4;
        }
        return BigDecimal.valueOf(percentagesFor(vehicleType)[tier], 2);
    }
}
