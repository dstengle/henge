package com.kenzan.henge.domain.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;

import org.junit.Test;


/**
 * Unit tests for {@link CheckReferenceCollectionValidator}
 *
 * @author wmatsushita
 */
public class CheckReferenceCollectionValidatorTest {

    @Test
    public void test() {

        final ModelReferenceExistsValidation validation = mock(ModelReferenceExistsValidation.class);
        final ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        
        final CheckModelReference checkModelReference = mock(CheckModelReference.class);
        
        doReturn(PropertyGroup.class).when(checkModelReference).value();
        
        when(validation.checkForExistence(any(), any(), any(), any())).thenReturn(true, true, true, false, true, false);
        
        CheckReferenceCollectionValidator validator = new CheckReferenceCollectionValidator(validation);
        validator.initialize(checkModelReference);
        
        final List<PropertyGroupReference> list = new ArrayList<>();
        
        for(int i=0; i<3; i++) {
            list.add(PropertyGroupReference.builder("name", "version").build());
        }
        
        assertTrue(validator.isValid(list, constraintValidatorContext));
        assertFalse(validator.isValid(list, constraintValidatorContext));
        
    }

}
