package com.mingeso.backend.dto;

import java.util.Map;

/**
 * Fila de los reportes R2 y R4: una reparacion con una celda por columna (tipo de vehiculo o de motor)
 * y el total de la fila.
 *
 * @param <K> enum de las columnas
 */
public record RepairMatrixRow<K extends Enum<K>>(
        Integer repairTypeId,
        String name,
        Map<K, CountAmount> cells,
        CountAmount total
) {
}
