package com.mingeso.backend.service.report;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class DescriptiveStatisticsTest {

    @Test
    void emptySeriesHasNullStatistics() {
        DescriptiveStatistics.Result result = DescriptiveStatistics.of(List.of());

        assertThat(result).isEqualTo(DescriptiveStatistics.Result.EMPTY);
        assertThat(result.count()).isZero();
        assertThat(result.average()).isNull();
        assertThat(result.standardDeviation()).isNull();
        assertThat(result.min()).isNull();
        assertThat(result.max()).isNull();
        assertThat(result.p90()).isNull();
    }

    @Test
    void singleValueIsEveryStatisticWithZeroDeviation() {
        DescriptiveStatistics.Result result = DescriptiveStatistics.of(List.of(5.0));

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.average()).isEqualByComparingTo("5.00");
        assertThat(result.standardDeviation()).isEqualByComparingTo("0.00");
        assertThat(result.min()).isEqualByComparingTo("5.00");
        assertThat(result.max()).isEqualByComparingTo("5.00");
        assertThat(result.p90()).isEqualByComparingTo("5.00");
    }

    @Test
    void computesStatisticsOfOneToTen() {
        // Promedio 5,5; varianza poblacional 8,25 -> desviacion 2,8723; P90 = posicion ceil(0,9 * 10) = 9
        List<Double> values = IntStream.rangeClosed(1, 10).mapToObj(i -> (double) i).toList();

        DescriptiveStatistics.Result result = DescriptiveStatistics.of(values);

        assertThat(result.count()).isEqualTo(10);
        assertThat(result.average()).isEqualByComparingTo("5.50");
        assertThat(result.standardDeviation()).isEqualByComparingTo("2.87");
        assertThat(result.min()).isEqualByComparingTo("1.00");
        assertThat(result.max()).isEqualByComparingTo("10.00");
        assertThat(result.p90()).isEqualByComparingTo("9.00");
    }

    @Test
    void sortsUnorderedInputBeforeComputing() {
        // Desviacion = raiz(2/3) = 0,8165; P90 = posicion ceil(0,9 * 3) = 3
        DescriptiveStatistics.Result result = DescriptiveStatistics.of(List.of(3.0, 1.0, 2.0));

        assertThat(result.average()).isEqualByComparingTo("2.00");
        assertThat(result.standardDeviation()).isEqualByComparingTo("0.82");
        assertThat(result.min()).isEqualByComparingTo("1.00");
        assertThat(result.max()).isEqualByComparingTo("3.00");
        assertThat(result.p90()).isEqualByComparingTo("3.00");
    }

    @Test
    void roundsResultsToTwoDecimalsHalfUp() {
        // 2,125 es exacto en binario: HALF_UP da 2,13 (HALF_EVEN daria 2,12)
        DescriptiveStatistics.Result result = DescriptiveStatistics.of(List.of(2.125));

        assertThat(result.average()).isEqualByComparingTo("2.13");
    }

    @Test
    void percentileNearestRankUsesCeilingOfRank() {
        List<Double> twenty = IntStream.rangeClosed(1, 20).mapToObj(i -> (double) i).toList();

        assertThat(DescriptiveStatistics.percentileNearestRank(twenty, 0.90)).isEqualTo(18.0);
        assertThat(DescriptiveStatistics.percentileNearestRank(twenty, 0.91)).isEqualTo(19.0);
        assertThat(DescriptiveStatistics.percentileNearestRank(twenty, 1.0)).isEqualTo(20.0);
        // Con p = 0 el rango seria 0; se toma el primer valor
        assertThat(DescriptiveStatistics.percentileNearestRank(twenty, 0.0)).isEqualTo(1.0);
    }
}
