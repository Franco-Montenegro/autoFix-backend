package com.mingeso.backend.dto;

import com.mingeso.backend.entity.EngineType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reporte R4: las 11 reparaciones (filas) vs los 4 tipos de motor (columnas),
 * con totales por fila, por columna y general. Solo cuenta ingresos entregados.
 */
public record R4ReportResponse(
        LocalDate from,
        LocalDate to,
        List<EngineType> engineTypes,
        List<RepairMatrixRow<EngineType>> rows,
        Map<EngineType, CountAmount> columnTotals,
        CountAmount grandTotal
) {
}
