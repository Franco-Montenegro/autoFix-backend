package com.mingeso.backend.service.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Estadisticas descriptivas de una serie de valores (R3).
 * Desviacion estandar poblacional (divide por n) y percentil 90 por nearest-rank.
 * Solo el resultado se redondea a 2 decimales (HALF_UP).
 */
public final class DescriptiveStatistics {

    private static final int SCALE = 2;
    private static final double P90 = 0.90;

    private DescriptiveStatistics() {
    }

    /** Sin valores, todas las estadisticas son null. */
    public record Result(
            int count,
            BigDecimal average,
            BigDecimal standardDeviation,
            BigDecimal min,
            BigDecimal max,
            BigDecimal p90
    ) {
        public static final Result EMPTY = new Result(0, null, null, null, null, null);
    }

    public static Result of(List<Double> values) {
        int n = values.size();
        if (n == 0) {
            return Result.EMPTY;
        }
        List<Double> sorted = values.stream().sorted().toList();

        double average = sorted.stream().mapToDouble(Double::doubleValue).sum() / n;
        double variance = sorted.stream()
                .mapToDouble(v -> (v - average) * (v - average))
                .sum() / n;

        return new Result(
                n,
                round(average),
                round(Math.sqrt(variance)),
                round(sorted.get(0)),
                round(sorted.get(n - 1)),
                round(percentileNearestRank(sorted, P90))
        );
    }

    /** Nearest-rank: el valor en la posicion ceil(p * n) de la serie ordenada (base 1). */
    static double percentileNearestRank(List<Double> sorted, double p) {
        int rank = (int) Math.ceil(p * sorted.size());
        return sorted.get(Math.max(rank, 1) - 1);
    }

    private static BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
