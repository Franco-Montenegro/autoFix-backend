package com.mingeso.backend.service.cost;

import com.mingeso.backend.entity.VehicleType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MileageSurchargePolicyTest {

    private final MileageSurchargePolicy policy = new MileageSurchargePolicy();

    /** Limites de cada tramo: 5.000/5.001, 12.000/12.001, 25.000/25.001, 40.000/40.001. */
    @ParameterizedTest(name = "{0} km, {1} -> {2}")
    @CsvSource({
            "0,      SEDAN,     0",    "5000,   SEDAN,     0",
            "5001,   SEDAN,     0.03", "12000,  SEDAN,     0.03",
            "12001,  SEDAN,     0.07", "25000,  SEDAN,     0.07",
            "25001,  SEDAN,     0.12", "40000,  SEDAN,     0.12",
            "40001,  SEDAN,     0.20", "150000, SEDAN,     0.20",
            "5000,   HATCHBACK, 0",    "5001,   HATCHBACK, 0.03", "12001, HATCHBACK, 0.07",
            "25001,  HATCHBACK, 0.12", "40001,  HATCHBACK, 0.20",
            "5000,   SUV,       0",    "5001,   SUV,       0.05", "12000, SUV,       0.05",
            "12001,  SUV,       0.09", "25000,  SUV,       0.09", "25001, SUV,       0.12",
            "40000,  SUV,       0.12", "40001,  SUV,       0.20",
            "5001,   PICKUP,    0.05", "12001,  PICKUP,    0.09", "40001, PICKUP,    0.20",
            "5001,   VAN,       0.05", "12001,  VAN,       0.09", "25001, VAN,       0.12"
    })
    void rateFollowsTable(int mileage, VehicleType vehicleType, BigDecimal expected) {
        assertThat(policy.rate(mileage, vehicleType)).isEqualByComparingTo(expected);
    }
}
