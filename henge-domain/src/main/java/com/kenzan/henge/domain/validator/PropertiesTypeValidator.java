package com.kenzan.henge.domain.validator;

import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.type.PropertyType;

public class PropertiesTypeValidator implements ConstraintValidator<CheckPropertiesType, Collection<? extends Property>>{

    @Override
    public void initialize(CheckPropertiesType constraintAnnotation) {
        // do nothing
    }

    @Override
    public boolean isValid(Collection<? extends Property> values, ConstraintValidatorContext context) {
        
        boolean valid = true;
        
        if(values != null) {

            property:
            for (Property value : values) {
                final PropertyType type = value.getType();
                if(type != null) {
        
                    // if default value is valid, process the scoped values
                    if(valid) {
                       for (PropertyScopedValue scopedValue : value.getPropertyScopedValues()) {
                           valid = type.validate(scopedValue.getValue());
                           // at any point, if any scoped value is invalid, skip next ones
                           if(!valid) {
                               break property;
                           }
                       }
                    }
                
                }
            }
            
        }
        
        return valid;
        
    }

}
