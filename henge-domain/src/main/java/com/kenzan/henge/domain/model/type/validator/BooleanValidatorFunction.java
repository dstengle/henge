package com.kenzan.henge.domain.model.type.validator;

import java.util.function.Function;

/**
 * 
 * @author geiser
 *
 */
public class BooleanValidatorFunction implements Function<String, Boolean>{

    @Override
    public Boolean apply(String t) {
        
        try {
            Boolean.valueOf(t);
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }

}
