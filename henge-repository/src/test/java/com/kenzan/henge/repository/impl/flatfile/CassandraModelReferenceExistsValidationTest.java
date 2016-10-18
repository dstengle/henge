package com.kenzan.henge.repository.impl.flatfile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.repository.impl.cassandra.BaseCassandraRepository;
import com.kenzan.henge.repository.impl.cassandra.CassandraModelReferenceExistsValidation;
import com.kenzan.henge.repository.impl.cassandra.CassandraRepositoryFactory;

import javax.validation.ConstraintValidatorContext;

import org.junit.Test;


/**
 * Unit tests for {@link CassandraModelReferenceExistsValidation}
 *
 * @author wmatsushita
 */
public class CassandraModelReferenceExistsValidationTest {

    /**
     * Test method for {@link com.kenzan.henge.repository.impl.cassandra.CassandraModelReferenceExistsValidation#checkForExistence(java.lang.Class, java.lang.String, java.lang.String, javax.validation.ConstraintValidatorContext)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testCheckForExistence() {

        final CassandraRepositoryFactory factory = mock(CassandraRepositoryFactory.class);
        final ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        
        final CassandraModelReferenceExistsValidation validation = new CassandraModelReferenceExistsValidation(factory);
        final BaseCassandraRepository repository = mock(BaseCassandraRepository.class);
        
        when(factory.get(PropertyGroup.class)).thenReturn(repository);
        
        when(repository.exists("name","version1")).thenReturn(true);
        when(repository.exists("name","version2")).thenReturn(false);
        
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
        
        assertTrue(validation.checkForExistence(PropertyGroup.class, "name", "version1", constraintValidatorContext));
        assertFalse(validation.checkForExistence(PropertyGroup.class, "name", "version2", constraintValidatorContext));
       
    }

}
