package com.mingeso.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

/**
 * Fecha y hora para registrar la salida o el retiro de un ingreso.
 */
public record RepairOrderDateRequest(

        @NotNull(message = "La fecha y hora es obligatoria")
        @PastOrPresent(message = "La fecha y hora no puede ser futura")
        LocalDateTime dateTime
) {
}
