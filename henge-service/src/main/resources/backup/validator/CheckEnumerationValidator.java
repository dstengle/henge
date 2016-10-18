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
public class CheckEnumerationValidator implements
        ConstraintValidator<CheckEnumeration, String> {
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
    public boolean isValid(String str,
            ConstraintValidatorContext constraintContext) {
        if (null == str) {
            return true;
        }

        return enumConstants.contains(str);
    }

}
