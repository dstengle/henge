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

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates that a String is equal (case and white-space sensitive) to a valid
 * enum constant.
 */
public class CheckEnumerationValidator implements ConstraintValidator<CheckEnumeration, String> {
    
	private ImmutableSet<String> enumConstants;

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.validation.ConstraintValidator#initialize(java.lang.annotation.
     * Annotation)
     */
    @Override
    public void initialize(CheckEnumeration checkEnumAnno) {
        checkNotNull(checkEnumAnno);
        checkNotNull(checkEnumAnno.value());
        Object[] enums = checkNotNull(checkEnumAnno.value().getEnumConstants());

        Set<String> strs = new HashSet<String>();
        for (Object obj : enums) {
            strs.add(checkNotNull(obj).toString());
        }

        enumConstants = ImmutableSet.copyOf(strs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
     * javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(String str, ConstraintValidatorContext constraintContext) {
        
    	if (null == str) {
            return true;
        }

        return enumConstants.contains(str);
    }

}
