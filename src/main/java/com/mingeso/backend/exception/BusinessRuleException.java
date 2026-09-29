package com.mingeso.backend.exception;

/**
 * La solicitud es valida en forma pero viola una regla de negocio (422).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
