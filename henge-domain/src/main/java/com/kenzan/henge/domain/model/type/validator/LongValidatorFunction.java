package com.kenzan.henge.domain.model.type.validator;

import java.util.function.Function;

/**
 * 
 * @author geiser
 *
 */
public class LongValidatorFunction implements Function<String, Boolean> {

    @Override
    public Boolean apply(String t) {

        try {
            Long.valueOf(t);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
