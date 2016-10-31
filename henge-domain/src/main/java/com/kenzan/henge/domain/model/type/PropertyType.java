/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenzan.henge.domain.model.type;

import java.util.function.Function;

import com.google.common.base.Strings;
import com.kenzan.henge.domain.model.type.validator.BooleanValidatorFunction;
import com.kenzan.henge.domain.model.type.validator.DoubleValidatorFunction;
import com.kenzan.henge.domain.model.type.validator.FloatValidatorFunction;
import com.kenzan.henge.domain.model.type.validator.IntegerValidatorFunction;
import com.kenzan.henge.domain.model.type.validator.LongValidatorFunction;

public enum PropertyType {

    STRING("String"), 
    BOOLEAN("Boolean", BooleanValidatorFunction.class), 
    INTEGER("Integer", IntegerValidatorFunction.class), 
    LONG("Long", LongValidatorFunction.class), 
    DOUBLE("Double", DoubleValidatorFunction.class), 
    FLOAT("Float", FloatValidatorFunction.class);

    private String type;
    private Class<Function<String, Boolean>> typeValidatorClass;

    private PropertyType(String type) {

        this(type, null);
    }

    @SuppressWarnings("unchecked")
    private PropertyType(String type, Class<? extends Function<String, Boolean>> typeValidatorClass) {

        this.type = type;
        this.typeValidatorClass = (Class<Function<String, Boolean>>) typeValidatorClass;
    }

    public String getType() {

        return this.type;
    }

    public boolean validate(String value) {

        if (!Strings.isNullOrEmpty(value) && this.typeValidatorClass != null) {
            Function<String, Boolean> function;
            try {
                function = this.typeValidatorClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(
                        String.format("Error creating type validator for class [%s]", this.typeValidatorClass));
            }
            return function.apply(value);
        }

        return true;
    }

}
