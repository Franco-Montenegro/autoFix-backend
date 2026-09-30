package com.mingeso.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Bono a asignar a un ingreso.
 */
public record BonusAssignRequest(

        @NotNull(message = "El id del bono es obligatorio")
        Long bonusId
) {
}
