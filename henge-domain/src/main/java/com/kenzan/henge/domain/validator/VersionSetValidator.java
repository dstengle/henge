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

import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.FileVersionReference;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * Validates that a Scope string is in the correct format.
 */
public class VersionSetValidator implements ConstraintValidator<CheckVersionSet, VersionSet> {

    private ModelReferenceExistsValidation validation;

    @Autowired
    public VersionSetValidator(ModelReferenceExistsValidation validation) {

        super();
        this.validation = validation;
    }
    
    @Override
    public void initialize(CheckVersionSet annotation) {
        checkNotNull(annotation);
    }

    @Override
    public boolean isValid(VersionSet model, ConstraintValidatorContext constraintContext) {
        
    	if (null == model) {
            return true;
        }
    	
    	boolean isValid = true;
        boolean validationResult;
    	if (!CollectionUtils.isEmpty(model.getPropertyGroupReferences())) {
    	    for (PropertyGroupReference pgRef : model.getPropertyGroupReferences()) {
    	        validationResult = validation.checkForExistence(PropertyGroup.class, pgRef.getName(), pgRef.getVersion(), constraintContext);
    	        isValid = validationResult ? isValid : validationResult; 
    	    }
    	}
    	
        if (!CollectionUtils.isEmpty(model.getFileVersionReferences())) {
            for (FileVersionReference fvRef : model.getFileVersionReferences()) {
                validationResult = validation.checkForExistence(FileVersion.class, fvRef.getName(), fvRef.getVersion(), constraintContext);
                isValid = validationResult ? isValid : validationResult; 
            }
        }
    	                
        return isValid;
    }

}
