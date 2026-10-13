package com.mingeso.backend.service.cost;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AttentionDayDiscountPolicyTest {

    private final AttentionDayDiscountPolicy policy = new AttentionDayDiscountPolicy();

    /** 2026-09-07 es lunes, 2026-09-08 martes y 2026-09-10 jueves. */
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "2026-09-07T09:00,    0.10",
            "2026-09-07T10:30,    0.10",
            "2026-09-07T12:00,    0.10",
            "2026-09-10T09:00,    0.10",
            "2026-09-10T12:00,    0.10",
            "2026-09-07T08:59:59, 0",
            "2026-09-07T12:00:01, 0",
            "2026-09-10T15:00,    0",
            "2026-09-08T10:00,    0",
            "2026-09-13T10:00,    0"
    })
    void appliesOnlyOnMondayAndThursdayFromNineToTwelve(LocalDateTime entryDateTime, BigDecimal expected) {
        assertThat(policy.rate(entryDateTime)).isEqualByComparingTo(expected);
    }
}
