package com.kenzan.henge.domain.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validation annotation that validates that a String is a valid annotation
 * value.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckEnumerationValidator.class)
@Documented
public @interface CheckEnumeration {
    /**
     * @return the error message if invalid
     */
    String message() default "not a valid enumeration value";

    /**
     * @return javax.validation groups
     */
    Class<?>[] groups() default {};

    /**
     * @return javax.validation payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * @return the enumeration class to validate
     */
    Class<?> value();
}
