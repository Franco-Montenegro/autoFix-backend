package com.mingeso.backend.dto;

/**
 * Estado de un ingreso. No se persiste: se deduce de las fechas de salida y retiro.
 */
public enum RepairOrderStatus {
    IN_REPAIR,
    READY,
    DELIVERED
}
