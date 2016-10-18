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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.AbstractBaseDomainTest;
import com.kenzan.henge.domain.utils.ScopeUtils;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PropertyScopedValueTest extends AbstractBaseDomainTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyScopedValueTest.class);
	
	@Autowired
	private Validator validator;
	
	@Autowired
	private ObjectMapper mapper;
	
//	@Test
//    public void serializesToJSON() throws Exception {
//		
//		final PropertyScopeValue propertyValue1 =  new PropertyScopeValue.Builder("key-1", "value-1").build(this.validator);
//		String propertyValue1JSON = readFile("fixtures/property_value.json", Charsets.UTF_8);
//		
//        final String expected = mapper.writeValueAsString(mapper.readValue(propertyValue1JSON, PropertyScopeValue.class));
//
//        assertThat(mapper.writeValueAsString(propertyValue1)).isEqualTo(expected);
//    }
//
//	@Test
//    public void deserializesFromJSON() throws Exception {
//		
//		final PropertyScopeValue propertyValue1 =  new PropertyScopeValue.Builder("key-1", "value-1").build(this.validator);		
//		String propertyValue1JSON = readFile("fixtures/property_value.json", Charsets.UTF_8);
//		
//        assertThat(mapper.readValue(propertyValue1JSON, PropertyScopeValue.class)).isEqualTo(propertyValue1);
//    }
	
	@Test
    public void toJSON() {
		
		try {
			
			// TOOD :: Add Properties			
			PropertyScopedValue propertyScopeValue1 = PropertyScopedValue.builder(ScopeUtils.parseScopeString("scope-key-1=scope-value-1"), "property-scope-value-1-value").
					build();
			
			try {
				LOGGER.info("PropertyScopeValue-1 :\n"+propertyScopeValue1+"\n");
				LOGGER.info("PropertyScopeValue-1 JSON :\n"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(propertyScopeValue1)+"\n\n");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
		} catch(ConstraintViolationException e) {
			fail("Not expecting ConstraintViolationException to be thrown here.");
		}
    }
	
	@Test
    public void validNamedPropertyValue() {
		try {
			PropertyScopedValue.builder(ScopeUtils.parseScopeString("scope-key-1=scope-value-1"), "some value").build(validator);
		} catch(ConstraintViolationException e) {
			fail("Not expecting ConstraintViolationException to be thrown here.");
		}

        
    }

    @Test
    public void keyAndValueNull() {
        try {
        	PropertyScopedValue.builder(null, null).build(validator);
            fail("Expected ConstraintViolationException wasn't thrown.");
        }
        catch (ConstraintViolationException e) {
        	assertThat(2).isEqualTo(e.getConstraintViolations().size());
        }
    }
    
//    private String readFile(String fileName, Charset charset) {
//    	try {
//    		return Resources.toString(Resources.getResource(fileName), charset).trim();
//    	}catch(IOException e) {
//    		throw new IllegalArgumentException(e);
//    	}
//    }
}
