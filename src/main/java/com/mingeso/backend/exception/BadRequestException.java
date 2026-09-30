package com.mingeso.backend.exception;

/**
 * Parametros de la solicitud invalidos que no cubre Bean Validation, ej. un rango de fechas invertido (400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
