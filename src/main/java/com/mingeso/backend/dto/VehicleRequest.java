package com.mingeso.backend.dto;

import com.mingeso.backend.dto.validation.ValidManufactureYear;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Datos para registrar un vehiculo nuevo.
 * La patente, la marca y el modelo se normalizan (trim + mayusculas) antes de validarse.
 */
public record VehicleRequest(

        @NotBlank(message = "La patente es obligatoria")
        @Pattern(regexp = "^[A-Z]{4}[0-9]{2}$", message = "La patente debe tener 4 letras seguidas de 2 números (ej. ABCD12)")
        String licensePlate,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca no puede superar los 50 caracteres")
        String brand,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 50, message = "El modelo no puede superar los 50 caracteres")
        String model,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        VehicleType vehicleType,

        @NotNull(message = "El año de fabricación es obligatorio")
        @ValidManufactureYear
        Integer manufactureYear,

        @NotNull(message = "El tipo de motor es obligatorio")
        EngineType engineType,

        @NotNull(message = "El número de asientos es obligatorio")
        @Min(value = 1, message = "El número de asientos debe ser al menos 1")
        @Max(value = 50, message = "El número de asientos no puede superar 50")
        Integer seats
) {
    public VehicleRequest {
        licensePlate = normalizeUpper(licensePlate);
        brand = normalizeUpper(brand);
        model = normalizeUpper(model);
    }

    public static String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
