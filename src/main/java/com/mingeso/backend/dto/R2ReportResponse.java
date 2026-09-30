package com.mingeso.backend.dto;

import com.mingeso.backend.entity.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reporte R2: las 11 reparaciones (filas) vs los 5 tipos de vehiculo (columnas),
 * con totales por fila, por columna y general. Solo cuenta ingresos entregados.
 */
public record R2ReportResponse(
        LocalDate from,
        LocalDate to,
        List<VehicleType> vehicleTypes,
        List<Row> rows,
        Map<VehicleType, Cell> columnTotals,
        Cell grandTotal
) {
    /** count = lineas de reparacion; amount = suma de sus precios de lista. */
    public record Cell(long count, BigDecimal amount) {

        public static final Cell EMPTY = new Cell(0, BigDecimal.ZERO);

        public Cell plus(Cell other) {
            return new Cell(count + other.count, amount.add(other.amount));
        }
    }

    public record Row(
            Integer repairTypeId,
            String name,
            Map<VehicleType, Cell> cells,
            Cell total
    ) {
    }
}
