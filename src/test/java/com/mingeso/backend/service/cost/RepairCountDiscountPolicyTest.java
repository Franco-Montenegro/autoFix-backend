package com.mingeso.backend.service.cost;

import com.mingeso.backend.entity.EngineType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RepairCountDiscountPolicyTest {

    private final RepairCountDiscountPolicy policy = new RepairCountDiscountPolicy();

    @ParameterizedTest
    @EnumSource(EngineType.class)
    void noPreviousRepairsHasNoDiscount(EngineType engineType) {
        assertThat(policy.rate(0, engineType)).isEqualByComparingTo("0");
    }

    /** Limites de cada tramo de la tabla del enunciado: 1-2, 3-5, 6-9, 10 o mas. */
    @ParameterizedTest(name = "{0} reparaciones previas, {1} -> {2}")
    @CsvSource({
            "1,  GASOLINE, 0.05", "2,  GASOLINE, 0.05", "3,  GASOLINE, 0.10", "5,  GASOLINE, 0.10",
            "6,  GASOLINE, 0.15", "9,  GASOLINE, 0.15", "10, GASOLINE, 0.20", "25, GASOLINE, 0.20",
            "1,  DIESEL,   0.07", "3,  DIESEL,   0.12", "6,  DIESEL,   0.17", "10, DIESEL,   0.22",
            "1,  HYBRID,   0.10", "3,  HYBRID,   0.15", "6,  HYBRID,   0.20", "10, HYBRID,   0.25",
            "1,  ELECTRIC, 0.08", "3,  ELECTRIC, 0.13", "6,  ELECTRIC, 0.18", "10, ELECTRIC, 0.23"
    })
    void rateFollowsTable(long previousRepairs, EngineType engineType, BigDecimal expected) {
        assertThat(policy.rate(previousRepairs, engineType)).isEqualByComparingTo(expected);
    }
}
