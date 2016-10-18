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
/**
 * 
 */
package com.kenzan.henge.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.AbstractBaseDomainTest;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.validation.ConstraintViolationException;

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
public class VersionSetTest extends AbstractBaseDomainTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetTest.class);
    
    private VersionSet expectedVersionSet;
    
    @Before
    public void prepare() {
        
        expectedVersionSet = VersionSet.builder("VersionSet-1", "1.0.1")
                        .withDescription("Version-Set-Description").withCreatedBy("createdBy").withCreatedDate(LocalDateTime.now())
                        .withPropertyGroupReferences(PropertyGroupReference.builder("property-group", "1.0.0").build())
                        .build();
    }

	@Test
    public void toJSON() {
		
		try {
			
			final VersionSet versionSet1 = VersionSet.builder("VersionSet-1", "1.0.1")
			                .withDescription("Version-Set-Description").withCreatedBy("createdBy").withCreatedDate(LocalDateTime.now())
			                .withPropertyGroupReferences(PropertyGroupReference.builder("property-group", "1.0.0").build())
			                .build();

			LOGGER.info("VersionSet-1 :\n"+versionSet1+"\n");
			LOGGER.info("VersionSet-1 JSON :\n"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(versionSet1)+"\n\n");

		} catch(ConstraintViolationException e) {
            LOGGER.error("Error to create a VersionSet json : ", e.getMessage());
            fail("Not expecting ConstraintViolationException to be thrown here.");
        } catch (JsonProcessingException e) {
            LOGGER.error("Error to create a VersionSet json : ", e.getMessage());
            fail("Not expecting JsonProcessingException to be thrown here.");
        }
    }
	
	@Test
    public void fromJSON() {
        
        //This json is the output from toJSON(). 
        String json = "{ \"name\" : \"VersionSet-1\", \"version\" : \"1.0.1\", \"description\" : \"Version-Set-Description\", \"createdDate\" : \"2016-03-17T16:27:53.212\","
            + " \"propertyGroupReferences\" : [ {   \"name\" : \"property-group\",   \"version\" : \"1.0.0\" } ],"
            + " \"scopedPropertyValueKeys\" : null, \"typeHierarchyEnabled\" : true }";
        
        try {
            
            final VersionSet versionSet = mapper.readValue(json, VersionSet.class);

            LOGGER.info("Expected versionSet: " + expectedVersionSet.toString());
            LOGGER.info("Deserialized versionSet: " + versionSet.toString());
            
            assertThat(versionSet).isEqualTo(expectedVersionSet);
            
            LOGGER.info("VersionSet was deserialized successfully: " + versionSet.toString());
        } catch (IOException e) {
            LOGGER.error("VersionSet deserialization failed : ", e.getMessage());
            fail("VersionSet deserialization failed.");
        }
    }
	
	@Test
    public void testImmutability() {
        
        final VersionSet.Builder builder = VersionSet.builder("name0", "1.0.0");
        final VersionSet pg = builder.build();
        
        builder.withName("name1").build();
        
        assertEquals("name0", pg.getName());
    }
	
	@Test
    public void testCopyObject() {
        
        final VersionSet copy = VersionSet.builder(expectedVersionSet).build();
        
        assertEquals(expectedVersionSet.getName(), copy.getName());
        assertEquals(expectedVersionSet.getCreatedBy(), copy.getCreatedBy());
        assertEquals(expectedVersionSet.getCreatedDate(), copy.getCreatedDate());
        assertEquals(expectedVersionSet.getDescription(), copy.getDescription());
        assertEquals(expectedVersionSet.getVersion(), copy.getVersion());
    }
	
}
