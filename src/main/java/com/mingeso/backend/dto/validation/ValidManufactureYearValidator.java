package com.mingeso.backend.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class ValidManufactureYearValidator implements ConstraintValidator<ValidManufactureYear, Integer> {

    static final int MIN_YEAR = 1900;

    /** Los modelos del año siguiente se venden desde el año actual. */
    static final int MAX_YEARS_AHEAD = 1;

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= MIN_YEAR && value <= Year.now().getValue() + MAX_YEARS_AHEAD;
    }
}
