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
package com.kenzan.henge.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.AbstractBaseDomainTest;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.domain.model.type.PropertyType;
import com.kenzan.henge.domain.utils.ScopeUtils;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.validation.ConstraintViolationException;

import jersey.repackaged.com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author kylebober
 * 
 * TODO :: Add additional validation tests cases
 * TODO :: Add test case for JSON serialization/deserialization
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
public class PropertyGroupTest extends AbstractBaseDomainTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyGroupTest.class);
	
	private PropertyGroup expectedPropertyGroup;
	
	@Before
	public void prepare() {
	    
		final Property property1 = new Property.Builder("app-name").
				withDescription("Application Description").
				withScopedValues(
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Pet Store App - Development").build(),
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Pet Store App - Production").build()							
				)
				.build();
		final Property property2 = new Property.Builder("property-foo").
				withDescription("Dummy Description").
				withScopedValues(
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Bar Dev").build(), 
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Bar Prod").build()
				).build();

		expectedPropertyGroup = PropertyGroup.builder("property-group-1", "1.0.0").
				withDescription("property-group-1-description").
				withType(PropertyGroupType.APP.name()).
				withIsActive(true).
				withCreatedBy("Kyle S. Bober").
				withCreatedDate(LocalDateTime.now()).
				withProperties(property1, property2).
				build();		
	}
	
	@Test
    public void testImmutability() {
        
        final PropertyGroup.Builder builder = PropertyGroup.builder("name0", "1.0.0");
        final PropertyGroup pg = builder.build();
        
        builder.withName("name1").build();
        
        assertEquals("name0", pg.getName());
    }
	
	@Test
    public void toJSON() {
		
		try {
		    LOGGER.info("Property-Group-1 :\n"+expectedPropertyGroup+"\n");
			LOGGER.info("Property-Group-1 JSON :\n"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedPropertyGroup)+"\n\n");
		} catch(ConstraintViolationException e) {
		    LOGGER.error("Error to create a PropertyGroup json : ", e.getMessage());
			fail("Not expecting ConstraintViolationException to be thrown here.");
		} catch (JsonProcessingException e) {
            LOGGER.error("Error to create a PropertyGroup json : ", e.getMessage());
            fail("Not expecting JsonProcessingException to be thrown here.");
        }
    }
	
	@Test
	public void fromJSON() {
		
		//This json is the output from toJSON(). 
		String json = "{  \"name\" : \"property-group-1\",  \"description\" : \"property-group-1-description\",  \"type\" : \"APP\",  \"active\" : true,  \"properties\" : [ {    \"name\" : \"app-name\",    \"description\" : \"Application Description\",    \"defaultValue\" : null,    \"propertyScopedValues\" : [ {      \"value\" : \"Pet Store App - Production\",      \"scopeSet\" : [ {        \"key\" : \"env\",        \"value\" : \"prod\"      } ]    }, {      \"value\" : \"Pet Store App - Development\",      \"scopeSet\" : [ {        \"key\" : \"env\",        \"value\" : \"dev\"      } ]    } ]  }, {    \"name\" : \"property-foo\",    \"description\" : \"Dummy Description\",    \"defaultValue\" : null,    \"propertyScopedValues\" : [ {      \"value\" : \"Bar Prod\",      \"scopeSet\" : [ {        \"key\" : \"env\",        \"value\" : \"prod\"      } ]    }, {      \"value\" : \"Bar Dev\",      \"scopeSet\" : [ {        \"key\" : \"env\",        \"value\" : \"dev\"      } ]    } ]  } ],  \"version\" : \"1.0.0\",  \"createdDate\" : \"2015-12-29T09:54:21.843\"}";
		
		try {
			PropertyGroup propertyGroup = mapper.readValue(json, PropertyGroup.class);

			LOGGER.info("Expected propertyGroup: " + expectedPropertyGroup.toString());
			LOGGER.info("Deserialized propertyGroup: " + propertyGroup.toString());
			
			assertThat(propertyGroup).isEqualTo(expectedPropertyGroup);
			
			LOGGER.info("PropertyGroup was deserialized successfully: " + propertyGroup.toString());
		} catch (IOException e) {
		    LOGGER.error("PropertyGroup deserialization failed : ", e.getMessage());
			fail("PropertyGroup deserialization failed.");
		}
	}
	
	@Test
	public void testValidation() {
	    
	    final Property property1 = new Property.Builder("app-name-1").
	                    withDescription("Application Description").
	                    withScopedValues(
	                            PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Pet Store App - Development").build(),
	                            PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Pet Store App - Production").build()                          
	                    )
	                    .build();
	    final Property property2 = new Property.Builder("property-foo-2").
	                    withDescription("Dummy Description").
	                    withScopedValues(
	                            PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Bar Dev").build(), 
	                            PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Bar Prod").build()
	                    ).build();
	    
	    final LocalDateTime now = LocalDateTime.now();
	    final PropertyGroup pg = PropertyGroup.builder("name", "1.0.0").withCreatedBy("createdBy").withCreatedDate(now)
	                    .withDescription("description").withIsActive(true)
	                    .withProperties(property1, property2).withType("APP").withVersion("1.0.0").build(validator);
	    
	    assertEquals("name", pg.getName());
	    assertEquals("createdBy", pg.getCreatedBy());
	    assertEquals(now, pg.getCreatedDate());
	    assertEquals("description", pg.getDescription());
	    assertTrue(pg.isActive());
	    assertTrue(pg.getProperties().containsAll(Sets.newHashSet(property1, property2)));
	    assertEquals("APP", pg.getType());
	    assertEquals("1.0.0", pg.getVersion());
	}
	
	@Test
    public void testValidationVersionFail() {
        
	    expectedException.expect(ConstraintViolationException.class);
	    
        final LocalDateTime now = LocalDateTime.now();
        PropertyGroup.builder("name", "1,0,0").withCreatedBy("createdBy").withCreatedDate(now)
                     .withDescription("description").withIsActive(true)
                     .withType("APP").build(validator);
    }
	
	@Test
    public void testValidationTypeFail() {
        
        expectedException.expect(ConstraintViolationException.class);
        
        final LocalDateTime now = LocalDateTime.now();
        PropertyGroup.builder("name", "1.0.0").withCreatedBy("createdBy").withCreatedDate(now)
                     .withDescription("description").withIsActive(true)
                     .withType("APPS").build(validator);
        fail("Expected ConstraintViolationException wasn't thrown.");
    }
	
    @Test
    public void testValidationPropertiesTypeFail() {

        expectedException.expect(ConstraintViolationException.class);

        final LocalDateTime now = LocalDateTime.now();
        PropertyGroup.builder("name", "1.0.0").withCreatedBy("createdBy").withCreatedDate(now)
                .withDescription("description").withIsActive(true).withType("APP")
                .withProperties(Property.builder("propertyName").withDefaultValue("1").withType(PropertyType.INTEGER)
                        .withScopedValues(PropertyScopedValue
                                .builder(Sets.newHashSet(Scope.builder("env", "dev").build()), "test").build())
                        .build())
                .build(validator);

        fail("Expected ConstraintViolationException wasn't thrown.");
    }

	@Test
    public void testCopyObject() {
        
        final Property property1 = new Property.Builder("app-name-1").
                        withDescription("Application Description").
                        withScopedValues(
                                PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Pet Store App - Development").build(),
                                PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Pet Store App - Production").build()                          
                        )
                        .build();
        final Property property2 = new Property.Builder("property-foo-2").
                        withDescription("Dummy Description").
                        withScopedValues(
                                PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Bar Dev").build(), 
                                PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Bar Prod").build()
                        ).build();
        
        final LocalDateTime now = LocalDateTime.now();
        final PropertyGroup original = PropertyGroup.builder("name", "1.0.0").withCreatedBy("createdBy").withCreatedDate(now)
                        .withDescription("description").withIsActive(true)
                        .withProperties(property1, property2).withType("APP").build(validator);
        
        final PropertyGroup copy = PropertyGroup.builder(original).build(validator);
        
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getCreatedBy(), copy.getCreatedBy());
        assertEquals(original.getCreatedDate(), copy.getCreatedDate());
        assertEquals(original.getDescription(), copy.getDescription());
        assertEquals(original.isActive(), copy.isActive());
        assertEquals(original.getProperties(), copy.getProperties());
        assertEquals(original.getType(), copy.getType());
        assertEquals(original.getVersion(), copy.getVersion());
    }
	
}
