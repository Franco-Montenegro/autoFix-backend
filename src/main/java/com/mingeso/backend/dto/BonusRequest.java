package com.mingeso.backend.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Cupo mensual de bonos de una marca (lo que informa TopCar a inicio de mes).
 * La marca se normaliza (trim + mayusculas) antes de validarse.
 */
public record BonusRequest(

        @NotBlank(message = "La marca es obligatoria")
        @Pattern(regexp = "^(TOYOTA|FORD|HYUNDAI|HONDA)$",
                message = "La marca debe ser una de: TOYOTA, FORD, HYUNDAI, HONDA")
        String brand,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 2000, message = "El año debe estar entre 2000 y 2100")
        @Max(value = 2100, message = "El año debe estar entre 2000 y 2100")
        Integer year,

        @NotNull(message = "El mes es obligatorio")
        @Min(value = 1, message = "El mes debe estar entre 1 y 12")
        @Max(value = 12, message = "El mes debe estar entre 1 y 12")
        Integer month,

        @NotNull(message = "La cantidad de bonos es obligatoria")
        @Min(value = 0, message = "La cantidad de bonos no puede ser negativa")
        Integer quantity,

        @NotNull(message = "El monto del bono es obligatorio")
        @Positive(message = "El monto del bono debe ser mayor que 0")
        @Digits(integer = 12, fraction = 0, message = "El monto del bono debe ser un número entero de pesos")
        BigDecimal amount
) {
    public BonusRequest {
        brand = VehicleRequest.normalizeUpper(brand);
    }
}
