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
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.AbstractBaseDomainTest;
import com.kenzan.henge.domain.utils.ScopeUtils;

import java.io.IOException;

import javax.validation.ConstraintViolationException;

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
public class PropertyScopeValueTest extends AbstractBaseDomainTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyGroupTest.class);
    
    @Test
    public void toJSON() {
		
		try {
			
			final PropertyScopedValue propertyScopeValue1 = PropertyScopedValue.builder(ScopeUtils.parseScopeString("scope-key-1=scope-value-1"), "property-scope-value-1-value").build();
			
			LOGGER.info("PropertyScopeValue-1 :\n"+propertyScopeValue1+"\n");
			LOGGER.info("PropertyScopeValue-1 JSON :\n"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(propertyScopeValue1)+"\n\n");
		} catch(ConstraintViolationException e) {
		    LOGGER.error("Error to create a PropertyScopedValue json : ", e.getMessage());
		    fail("Not expecting ConstraintViolationException to be thrown here.");
		} catch (JsonProcessingException e) {
		    LOGGER.error("Error to create a PropertyScopedValue json : ", e.getMessage());
            fail("Not expecting JsonProcessingException to be thrown here.");
        }
    }
    
    @Test
    public void fromJSON() {
        
        final PropertyScopedValue expectedPropertyScopedValue = PropertyScopedValue.builder(ScopeUtils.parseScopeString("scope-key-1=scope-value-1"),
                                                                                                    "property-scope-value-1-value").build();
        //This json is the output from toJSON(). 
        String json = "{  \"value\" : \"property-scope-value-1-value\", \"scopeSet\" : [ { \"key\" : \"scope-key-1\", \"value\" : \"scope-value-1\" } ] }";
        
        try {
            final PropertyScopedValue propertyScopedValue = mapper.readValue(json, PropertyScopedValue.class);

            LOGGER.info("Expected propertyScopedValue: " + expectedPropertyScopedValue.toString());
            LOGGER.info("Deserialized propertyScopedValue: " + propertyScopedValue.toString());
            
            assertThat(propertyScopedValue).isEqualTo(expectedPropertyScopedValue);
            
            LOGGER.info("PropertyScopedValue was deserialized successfully: " + propertyScopedValue.toString());
        } catch (IOException e) {
            LOGGER.error("PropertyScopedValue deserialization failed : ", e.getMessage());
            fail("PropertyScopedValue deserialization failed.");
        }
    }
    
	@Test
    public void validNamedPropertyValue() {
	    
		try {
			PropertyScopedValue.builder(ScopeUtils.parseScopeString("scope-key-1=scope-value-1"), "some value").build(validator);
		} catch(ConstraintViolationException e) {
		    LOGGER.error("PropertyScopedValue valid named failed : ", e.getMessage());
			fail("Not expecting ConstraintViolationException to be thrown here.");
		}
    }

	@Test
    public void keyAndValueNull() {

	    expectedException.expect(ConstraintViolationException.class);
	    
	    PropertyScopedValue.builder(null, null).build(validator);
        fail("Expected ConstraintViolationException wasn't thrown.");
    }
    
}
