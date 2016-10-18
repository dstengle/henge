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
package com.kenzan.henge.domain.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.kenzan.henge.domain.model.NamedVersionedModelReference;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validates that a {@link NamedVersionedModelReference} points to an existing object in the
 * respository.
 */
public class CheckReferenceValidator implements ConstraintValidator<CheckModelReference, NamedVersionedModelReference> {
    
	@Autowired
	private ModelReferenceExistsValidation validation;
	
	private Class<?> modelType;

    @Override
    public void initialize(final CheckModelReference validationAnnotation) {
        
        checkNotNull(validationAnnotation);
        checkNotNull(validationAnnotation.value());
        modelType = validationAnnotation.value();
    }

    @Override
    public boolean isValid(final NamedVersionedModelReference model, final ConstraintValidatorContext constraintContext) {
        
        return validation.checkForExistence(modelType, model.getName(), model.getVersion(), constraintContext);
    }

}
