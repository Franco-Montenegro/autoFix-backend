package com.mingeso.backend.dto;

import com.mingeso.backend.entity.VehicleType;

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
        List<RepairMatrixRow<VehicleType>> rows,
        Map<VehicleType, CountAmount> columnTotals,
        CountAmount grandTotal
) {
}
