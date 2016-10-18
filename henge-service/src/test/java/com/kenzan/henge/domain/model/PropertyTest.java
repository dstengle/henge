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

import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kenzan.henge.config.TestContextConfig;

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
public class PropertyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyTest.class);
    
	@Autowired
	private Validator validator;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Test
    public void toJSON() {
		
		try {
			
			// TOOD :: Add PropertyGroup and PropertyScopeValues			
			Property property1 = new Property.Builder("property-1").
					withDefaultValue("property-1-default-value").withDescription("property-1-description").
					build();
			
			try {
				LOGGER.info("Property-1 :\n"+property1+"\n");
				LOGGER.info("Property-1 JSON :\n"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(property1)+"\n\n");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
		} catch(ConstraintViolationException e) {
			fail("Not expecting ConstraintViolationException to be thrown here.");
		}
    }
}
