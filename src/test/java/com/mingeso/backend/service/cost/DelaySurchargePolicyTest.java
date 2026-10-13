package com.mingeso.backend.service.cost;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DelaySurchargePolicyTest {

    private final DelaySurchargePolicy policy = new DelaySurchargePolicy();

    /** Se cuentan dias calendario entre la fecha de salida y la de retiro, sin importar la hora. */
    @ParameterizedTest(name = "salida {0}, retiro {1} -> {2} dias, {3}")
    @CsvSource({
            "2026-09-10T10:00, 2026-09-10T18:00, 0, 0",
            "2026-09-10T23:00, 2026-09-11T08:00, 1, 0.05",
            "2026-09-10T08:00, 2026-09-11T20:00, 1, 0.05",
            "2026-09-10T10:00, 2026-09-13T10:00, 3, 0.15",
            "2026-09-25T10:00, 2026-10-05T10:00, 10, 0.50"
    })
    void chargesFivePercentPerCalendarDay(LocalDateTime ready, LocalDateTime pickup,
                                          long expectedDays, BigDecimal expectedRate) {
        assertThat(policy.delayDays(ready, pickup)).isEqualTo(expectedDays);
        assertThat(policy.rate(ready, pickup)).isEqualByComparingTo(expectedRate);
    }

    @ParameterizedTest
    @CsvSource("2026-09-12T10:00, 2026-09-10T10:00")
    void pickupBeforeReadyNeverProducesNegativeDays(LocalDateTime ready, LocalDateTime pickup) {
        assertThat(policy.delayDays(ready, pickup)).isZero();
        assertThat(policy.rate(ready, pickup)).isEqualByComparingTo("0");
    }
}
