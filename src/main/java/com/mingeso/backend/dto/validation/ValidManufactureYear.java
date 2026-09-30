package com.mingeso.backend.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * El año de fabricacion debe estar entre 1900 y el año siguiente al actual
 * (ej. un modelo 2027 vendido en 2026).
 * El limite superior cambia cada año, por eso no se usa @Max.
 * Un valor null se considera valido (combinar con @NotNull).
 */
@Documented
@Constraint(validatedBy = ValidManufactureYearValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidManufactureYear {

    String message() default "El año de fabricación debe estar entre 1900 y el año siguiente al actual";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
