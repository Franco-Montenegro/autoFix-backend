package com.mingeso.backend.service.cost;

import com.mingeso.backend.entity.VehicleType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AgeSurchargePolicyTest {

    private final AgeSurchargePolicy policy = new AgeSurchargePolicy();

    /** Limites de cada tramo: 0-5, 6-10, 11-15, 16 o mas. Una antiguedad negativa cuenta como 0. */
    @ParameterizedTest(name = "{0} años, {1} -> {2}")
    @CsvSource({
            "-1, SEDAN,     0",
            "0,  SEDAN,     0",    "5,  SEDAN,     0",
            "6,  SEDAN,     0.05", "10, SEDAN,     0.05",
            "11, SEDAN,     0.09", "15, SEDAN,     0.09",
            "16, SEDAN,     0.15", "40, SEDAN,     0.15",
            "6,  HATCHBACK, 0.05", "11, HATCHBACK, 0.09", "16, HATCHBACK, 0.15",
            "5,  SUV,       0",    "6,  SUV,       0.07", "10, SUV,       0.07",
            "11, SUV,       0.11", "15, SUV,       0.11", "16, SUV,       0.20",
            "6,  PICKUP,    0.07", "11, PICKUP,    0.11", "16, PICKUP,    0.20",
            "6,  VAN,       0.07", "11, VAN,       0.11", "16, VAN,       0.20"
    })
    void rateFollowsTable(int ageInYears, VehicleType vehicleType, BigDecimal expected) {
        assertThat(policy.rate(ageInYears, vehicleType)).isEqualByComparingTo(expected);
    }
}
