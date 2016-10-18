package com.kenzan.henge.repository.impl.flatfile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

import javax.validation.ConstraintValidatorContext;

import org.junit.Test;


/**
 * Unit tests for {@link FlatFileModelReferenceExistsValidation}
 *
 * @author wmatsushita
 */
public class FlatFileModelReferenceExistsValidationTest {

    /**
     * Test method for {@link com.kenzan.henge.repository.impl.flatfile.FlatFileModelReferenceExistsValidation#checkForExistence(java.lang.Class, java.lang.String, java.lang.String, javax.validation.ConstraintValidatorContext)}.
     */
    @Test
    public void testCheckForExistence() {

        final FileStorageService storageService = mock(FileStorageService.class);
        final FileNamingService namingService = mock(FileNamingService.class);
        final ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        
        final FlatFileModelReferenceExistsValidation validation = new FlatFileModelReferenceExistsValidation(storageService, namingService);
        
        when(namingService.getPath(PropertyGroup.class)).thenReturn("PropertyGroup");
        when(namingService.getPath(VersionSet.class)).thenReturn("VersionSet");        
        when(namingService.getCompleteFileName("name", "version")).thenReturn("name_version");
        
        when(storageService.exists("PropertyGroup", "name_version")).thenReturn(true);
        when(storageService.exists("VersionSet", "name_version")).thenReturn(false);
        
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
        
        assertTrue(validation.checkForExistence(PropertyGroup.class, "name", "version", constraintValidatorContext));
        assertFalse(validation.checkForExistence(VersionSet.class, "name", "version", constraintValidatorContext));
       
    }

}
