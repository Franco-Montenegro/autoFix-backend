package com.mingeso.backend.service.cost;

import com.mingeso.backend.entity.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Recargo por antiguedad (año de ingreso - año de fabricacion) segun el tipo de vehiculo.
 * Tramos: 0-5, 6-10, 11-15, 16 o mas. Una antiguedad negativa (modelo del año siguiente) cuenta como 0.
 */
@Component
public class AgeSurchargePolicy {

    private static int[] percentagesFor(VehicleType vehicleType) {
        return switch (vehicleType) {
            case SEDAN, HATCHBACK -> new int[]{0, 5, 9, 15};
            case SUV, PICKUP, VAN -> new int[]{0, 7, 11, 20};
        };
    }

    public BigDecimal rate(int ageInYears, VehicleType vehicleType) {
        int tier;
        if (ageInYears <= 5) {
            tier = 0;
        } else if (ageInYears <= 10) {
            tier = 1;
        } else if (ageInYears <= 15) {
            tier = 2;
        } else {
            tier = 3;
        }
        return BigDecimal.valueOf(percentagesFor(vehicleType)[tier], 2);
    }
}
