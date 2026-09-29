package com.mingeso.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato comun de error de la API.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        List<FieldError> errors
) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, message, List.of());
    }

    public static ErrorResponse of(int status, String message, List<FieldError> errors) {
        return new ErrorResponse(LocalDateTime.now(), status, message, errors);
    }
}
