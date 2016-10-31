package com.kenzan.henge.domain.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { PropertiesTypeValidator.class })
@Documented
public @interface CheckPropertiesType {

    String message() default "one or more property values are not assignable to the defined type";
    
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
