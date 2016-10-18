package com.kenzan.henge.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.domain.AbstractBaseDomainTest;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.exception.HengeParseException;

import java.io.IOException;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.collect.Sets;

/**
 * Validate all static methods that we have on Scope class
 * 
 * @author Igor K. Shiohara
 *
 */
public class ScopeTest extends AbstractBaseDomainTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ScopeTest.class);
	
	private static final String SCOPE1 = "env=dev,stack=sbnet,region=dev-us-west2";
	private static final String WRONG_SCOPE_FORMAT = "env:dev,stack=sbnet,region=dev-us-west2";

	/**
	 * Success validation
	 */
	@Test
	public void splitScopesTest() {
	    
		final Set<Scope> scopes = ScopeUtils.parseScopeString(SCOPE1);
		assertNotNull(scopes);
		assertFalse(scopes.isEmpty());
		
		final Set<Scope> validate = Sets.newHashSet(
					Scope.builder("env", "dev").build(),
					Scope.builder("stack", "sbnet").build(),
					Scope.builder("region", "dev-us-west2").build());
		
		final Set<Scope> validate2 = Sets.newHashSet(
				Scope.builder("stack", "sbnet").build(),
				Scope.builder("region", "dev-us-west2").build(),
				Scope.builder("env", "dev").build());
		
		assertEquals(validate, scopes);
		assertEquals(validate2, scopes);
	}
	
	@Test
	public void wrongScopeFormatTest() {
	    
	    expectedException.expect(HengeParseException.class);
		ScopeUtils.parseScopeString(WRONG_SCOPE_FORMAT);
	}
	
	@Test
    public void validationSuccess() {
        
        try {
            Scope.builder("scope-key-1", "scope-value-1").build(validator);
        } catch(ConstraintViolationException e) {
            LOGGER.error("Scope validation failed : ", e.getMessage());
            fail("Not expecting ConstraintViolationException to be thrown here.");
        }
    }
	
	@Test
    public void toJSON() {
        
        try {
            
            final Scope scope = Scope.builder("scope-key-1", "scope-value-1").build(validator);
            
            LOGGER.info("Scope :\n"+scope+"\n");
            LOGGER.info("Scope JSON :\n"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(scope)+"\n\n");
        } catch(ConstraintViolationException e) {
            LOGGER.error("Error to create a Scope json : ", e.getMessage());
            fail("Not expecting ConstraintViolationException to be thrown here.");
        } catch (JsonProcessingException e) {
            LOGGER.error("Error to create a Scope json : ", e.getMessage());
            fail("Not expecting JsonProcessingException to be thrown here.");
        }
    }
    
    @Test
    public void fromJSON() {
        
        final Scope expectedScope = Scope.builder("scope-key-1", "scope-value-1").build(validator);
        
        //This json is the output from toJSON(). 
        String json = "{ \"key\" : \"scope-key-1\", \"value\" : \"scope-value-1\" }";
        
        try {
            final Scope scope = mapper.readValue(json, Scope.class);

            LOGGER.info("Expected scope: " + expectedScope.toString());
            LOGGER.info("Deserialized scope: " + scope.toString());
            
            assertThat(scope).isEqualTo(expectedScope);
            
            LOGGER.info("Scope was deserialized successfully: " + scope.toString());
        } catch (IOException e) {
            LOGGER.error("Scope deserialization failed : ", e.getMessage());
            fail("Scope deserialization failed.");
        }
    }
    
    @Test
    public void keyAndValueNull() {

        expectedException.expect(ConstraintViolationException.class);
        
        Scope.builder(null, null).build(validator);
        fail("Expected ConstraintViolationException wasn't thrown.");
    }
    
    @Test
    public void testCopyObject() {
        
        final Scope original = Scope.builder("key", "value").build();
        
        final Scope copy = Scope.builder(original).build();
        
        assertEquals(original.getKey(), copy.getKey());
        assertEquals(original.getValue(), copy.getValue());
    }

}
