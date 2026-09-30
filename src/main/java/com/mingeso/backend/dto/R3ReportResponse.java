package com.mingeso.backend.dto;

import com.mingeso.backend.entity.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte R3: estadisticas de los tiempos reales de reparacion (salida - ingreso) por tipo de vehiculo.
 * Siempre trae los 5 tipos; los que no tienen datos vienen con count 0 y estadisticas null.
 */
public record R3ReportResponse(
        LocalDate from,
        LocalDate to,
        String unit,
        List<Row> rows
) {
    public record Row(
            VehicleType vehicleType,
            int count,
            BigDecimal average,
            BigDecimal standardDeviation,
            BigDecimal min,
            BigDecimal max,
            BigDecimal p90
    ) {
    }
}
