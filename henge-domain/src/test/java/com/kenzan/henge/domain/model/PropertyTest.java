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
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.kenzan.henge.config.JacksonConfig;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.config.ValidatorConfig;
import com.kenzan.henge.domain.AbstractBaseDomainTest;
import com.kenzan.henge.domain.fixture.PropertyFixture;
import com.kenzan.henge.domain.model.type.PropertyType;
import com.kenzan.henge.domain.utils.ScopeUtils;

import java.io.IOException;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author kylebober
 * 
 *         TODO :: Add additional validation tests cases TODO :: Add test case
 *         for JSON serialization/deserialization
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
public class PropertyTest extends AbstractBaseDomainTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyGroupTest.class);

	private PropertyFixture fixture = new PropertyFixture();
	
	private Property property;
	
	//env;env+region;env+region+stack;hostname;application
	private static final ImmutableList<ImmutableSet<String>> SCOPE_PRECEDENCE_CONFIG = ImmutableList.of(
																	ImmutableSet.of("env"), 
																	ImmutableSet.of("env", "region"),
																	ImmutableSet.of("env", "region", "stack"),
																	ImmutableSet.of("hostname"),
																	ImmutableSet.of("application")
															);
	
	
	@Before
	public void setup() {
		
		property = new Property.Builder("property.name").withDefaultValue("default-value").withType(PropertyType.STRING)
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-1,stack=stack-1,subenv=subenv-1"),	"env=env-1,stack=stack-1,subenv=subenv-1--value").build())
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-1,stack=stack-1,subenv=subenv-2"),	"env=env-1,stack=stack-1,subenv=subenv-2--value").build())
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-1,stack=stack-2,subenv=subenv-1"), "env=env-1,stack=stack-2,subenv=subenv-1--value").build())
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-1,stack=stack-1"), "env=env-1,stack=stack-1--value").build())
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-1,stack=stack-2"), "env=env-1,stack=stack-2--value").build())
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-1,stack=stack-3"), "env=env-1,stack=stack-3--value").build())
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-1"), "env=env-1--value").build())
				.withScopedValues(PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=env-2"), "env=env-2--value").build()).build();
	}

	@Test
	public void toJSON() {

		try {

		    LOGGER.info("Property :\n" + property + "\n");
			LOGGER.info("Property JSON :\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(property) + "\n\n");
			
		} catch (ConstraintViolationException e) {
		    LOGGER.error("Error to create a Property json : ", e.getMessage());
			fail("Not expecting ConstraintViolationException to be thrown here.");
		} catch (JsonProcessingException e) {
		    LOGGER.error("Error to create a Property json : ", e.getMessage());
            fail("Not expecting JsonProcessingException to be thrown here.");
        }
	}
	
	@Test
    public void fromJSON() {
        
        //This json is the output from toJSON(). 
        String json = "{ \"name\" : \"property.name\", \"description\" : null, \"defaultValue\" : \"default-value\", \"type\" : \"STRING\", \"propertyScopedValues\" :"
            + " [ { \"value\" : \"env=env-1,stack=stack-1,subenv=subenv-2--value\", \"scopeSet\" : [ { \"key\" : \"subenv\", \"value\" : \"subenv-2\" },"
            + " { \"key\" : \"stack\", \"value\" : \"stack-1\" }, { \"key\" : \"env\", \"value\" : \"env-1\" } ] }, { \"value\" : "
            + "\"env=env-1,stack=stack-2,subenv=subenv-1--value\", \"scopeSet\" : [ { \"key\" : \"subenv\", \"value\" : \"subenv-1\" },"
            + " { \"key\" : \"stack\", \"value\" : \"stack-2\" }, { \"key\" : \"env\", \"value\" : \"env-1\" } ] }, { \"value\" :"
            + " \"env=env-1,stack=stack-1--value\", \"scopeSet\" : [ { \"key\" : \"stack\", \"value\" : \"stack-1\" }, { \"key\" :"
            + " \"env\", \"value\" : \"env-1\" } ] }, { \"value\" : \"env=env-1,stack=stack-1,subenv=subenv-1--value\", \"scopeSet\" : "
            + "[ { \"key\" : \"subenv\", \"value\" : \"subenv-1\" }, { \"key\" : \"stack\", \"value\" : \"stack-1\" }, { \"key\" : \"env\", "
            + "\"value\" : \"env-1\" } ] }, { \"value\" : \"env=env-1--value\", \"scopeSet\" : [ { \"key\" : \"env\", \"value\" : \"env-1\" } ] }, "
            + "{ \"value\" : \"env=env-1,stack=stack-2--value\", \"scopeSet\" : [ { \"key\" : \"stack\", \"value\" : \"stack-2\" }, "
            + "{ \"key\" : \"env\", \"value\" : \"env-1\" } ] }, { \"value\" : \"env=env-2--value\", \"scopeSet\" : "
            + "[ { \"key\" : \"env\", \"value\" : \"env-2\" } ] }, { \"value\" : \"env=env-1,stack=stack-3--value\", \"scopeSet\" : "
            + "[ { \"key\" : \"stack\", \"value\" : \"stack-3\" }, { \"key\" : \"env\", \"value\" : \"env-1\" } ] } ] }";
        
        try {
            
            final Property newProperty = mapper.readValue(json, Property.class);

            LOGGER.info("Expected property: " + property.toString());
            LOGGER.info("Deserialized property: " + newProperty.toString());
            
            assertThat(newProperty).isEqualTo(property);
            
            LOGGER.info("Property was deserialized successfully: " + newProperty.toString());
        } catch (IOException e) {
            LOGGER.error("Property deserialization failed : ", e.getMessage());
            fail("Property deserialization failed.");
        }
    }
	
	@Test		
	public void getScopeValueExactMatchTest() {
	    
		final Property p1 = fixture.getProperty1();
		final String exactMatch = ScopeUtils.getScopeValue(p1, Sets.newHashSet(
					Scope.builder("env", "dev").build(),
					Scope.builder("region", "region1").build()
				), SCOPE_PRECEDENCE_CONFIG)[1];
		
		assertEquals("end-dev-region-1-value", exactMatch);
	}
	
	@Test		
	public void getScopeValueDifferentRegionTest() {
		
	    final Property p1 = fixture.getProperty1();
		final String scopeValue = ScopeUtils.getScopeValue(p1, Sets.newHashSet(
					Scope.builder("env", "dev").build(),
					Scope.builder("region", "wrong-region").build()
				), SCOPE_PRECEDENCE_CONFIG)[1];
		
		assertEquals("end-dev-value", scopeValue);
	}
	
	@Test		
	public void getScopeValueCorrectRegionButWrongEnvTest() {
		
	    final Property p1 = fixture.getProperty1();
		final String scopeValue = ScopeUtils.getScopeValue(p1, Sets.newHashSet(
					Scope.builder("env", "wrong-env").build(),
					Scope.builder("region", "region1").build()
				), SCOPE_PRECEDENCE_CONFIG)[1];
		
		assertEquals("DefaultValue1", scopeValue);
	}
	
	@Test		
	public void getScopeValueEnvRegionStackTest() {
	    
	    final Property p1 = fixture.getProperty1();
		final String scopeValue = ScopeUtils.getScopeValue(p1, Sets.newHashSet(
					Scope.builder("env", "dev").build(),
					Scope.builder("region", "region1").build(),
					Scope.builder("stack", "stack1").build()
				), SCOPE_PRECEDENCE_CONFIG)[1];
		
		assertEquals("end-dev-stack-1-region-1", scopeValue);
	}
	
	@Test		
	public void getScopeValueDifferentEnvTest() {
		
	    final Property p1 = fixture.getProperty1();
		final String scopeValue = ScopeUtils.getScopeValue(p1, Sets.newHashSet(
					Scope.builder("env", "wrong-dev").build(),
					Scope.builder("region", "region1").build(),
					Scope.builder("stack", "stack2").build()
				), SCOPE_PRECEDENCE_CONFIG)[1];
		
		assertEquals("DefaultValue1", scopeValue);
	}
	
	@Test		
	public void getScopeValueApplicationTest() {
		
	    final Property p1 = fixture.getProperty1();
		final String scopeValue = ScopeUtils.getScopeValue(p1, Sets.newHashSet(
					Scope.builder("application", "app1").build()
				), SCOPE_PRECEDENCE_CONFIG)[1];
		
		assertEquals("DefaultValue1", scopeValue);
	}
	
	@Test
    public void testCopyObject() {
        
        final Property copy = Property.builder(property).build();
        
        assertEquals(property.getName(), copy.getName());
        assertEquals(property.getDefaultValue(), copy.getDefaultValue());
        assertEquals(property.getPropertyScopedValues(), copy.getPropertyScopedValues());
        assertEquals(property.getDescription(), copy.getDescription());
    }

}
