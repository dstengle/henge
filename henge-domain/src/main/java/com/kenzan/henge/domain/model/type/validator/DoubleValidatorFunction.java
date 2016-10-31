package com.kenzan.henge.domain.model.type.validator;

import java.util.function.Function;

/**
 * 
 * @author geiser
 *
 */
public class DoubleValidatorFunction implements Function<String, Boolean> {

    @Override
    public Boolean apply(String t) {

        try {
            Double.valueOf(t);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
