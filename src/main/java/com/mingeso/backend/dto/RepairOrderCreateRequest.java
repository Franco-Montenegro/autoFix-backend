package com.mingeso.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos para registrar el ingreso de un vehiculo al taller.
 */
public record RepairOrderCreateRequest(

        @NotBlank(message = "La patente es obligatoria")
        @Pattern(regexp = "^[A-Z]{4}[0-9]{2}$", message = "La patente debe tener 4 letras seguidas de 2 números (ej. ABCD12)")
        String licensePlate,

        @NotNull(message = "La fecha y hora de ingreso es obligatoria")
        @PastOrPresent(message = "La fecha y hora de ingreso no puede ser futura")
        LocalDateTime entryDateTime,

        @NotNull(message = "El kilometraje es obligatorio")
        @Min(value = 0, message = "El kilometraje no puede ser negativo")
        Integer mileage,

        @NotEmpty(message = "Debe indicar al menos una reparación")
        @UniqueElements(message = "No se puede repetir una reparación en el mismo ingreso")
        List<@NotNull(message = "El id de reparación no puede ser nulo") Integer> repairTypeIds
) {
    public RepairOrderCreateRequest {
        licensePlate = VehicleRequest.normalizeUpper(licensePlate);
    }
}
