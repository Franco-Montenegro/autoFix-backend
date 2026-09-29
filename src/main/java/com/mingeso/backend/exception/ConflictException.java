package com.mingeso.backend.exception;

/**
 * La operacion choca con el estado actual de los datos, ej. patente duplicada (409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
