package com.mingeso.backend.service.report;

import com.mingeso.backend.dto.CountAmount;
import com.mingeso.backend.dto.RepairMatrixRow;
import com.mingeso.backend.entity.RepairType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma la matriz "reparaciones vs columnas" de R2 (tipos de vehiculo) y R4 (tipos de motor).
 * Siempre incluye todas las reparaciones y todas las columnas (con 0 si no hay datos),
 * calcula totales por fila, por columna y general, y ordena las filas por monto total descendente
 * (empates por id de reparacion).
 */
public final class RepairMatrixBuilder {

    private RepairMatrixBuilder() {
    }

    /** Lo que devuelve la BD para una combinacion (reparacion, columna). */
    public record Aggregate<K>(Integer repairTypeId, K column, long count, BigDecimal amount) {
    }

    public record Result<K extends Enum<K>>(
            List<RepairMatrixRow<K>> rows,
            Map<K, CountAmount> columnTotals,
            CountAmount grandTotal
    ) {
    }

    public static <K extends Enum<K>> Result<K> build(List<RepairType> repairTypes,
                                                      Class<K> columnType,
                                                      List<Aggregate<K>> aggregates) {
        Map<Integer, Map<K, CountAmount>> found = new HashMap<>();
        for (Aggregate<K> agg : aggregates) {
            found.computeIfAbsent(agg.repairTypeId(), id -> new EnumMap<>(columnType))
                    .put(agg.column(), new CountAmount(agg.count(), agg.amount()));
        }

        Map<K, CountAmount> columnTotals = emptyCells(columnType);
        CountAmount grandTotal = CountAmount.EMPTY;
        List<RepairMatrixRow<K>> rows = new ArrayList<>();

        for (RepairType repairType : repairTypes) {
            Map<K, CountAmount> cells = emptyCells(columnType);
            cells.putAll(found.getOrDefault(repairType.getId(), Map.of()));

            CountAmount rowTotal = cells.values().stream().reduce(CountAmount.EMPTY, CountAmount::plus);
            cells.forEach((column, cell) -> columnTotals.merge(column, cell, CountAmount::plus));
            grandTotal = grandTotal.plus(rowTotal);

            rows.add(new RepairMatrixRow<>(repairType.getId(), repairType.getName(), cells, rowTotal));
        }

        rows.sort(Comparator
                .comparing((RepairMatrixRow<K> row) -> row.total().amount()).reversed()
                .thenComparing(RepairMatrixRow::repairTypeId));

        return new Result<>(rows, columnTotals, grandTotal);
    }

    private static <K extends Enum<K>> Map<K, CountAmount> emptyCells(Class<K> columnType) {
        Map<K, CountAmount> cells = new EnumMap<>(columnType);
        for (K column : columnType.getEnumConstants()) {
            cells.put(column, CountAmount.EMPTY);
        }
        return cells;
    }
}
