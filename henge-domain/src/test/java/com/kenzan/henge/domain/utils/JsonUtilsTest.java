package com.kenzan.henge.domain.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.type.PropertyType;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
public class JsonUtilsTest {
	
	@Autowired
	private JsonUtils jsonUtils;
	
	@Test
	public void toJsonTest() throws JsonProcessingException {
		
	    final String expected = "{\"name\":\"propertyName\",\"description\":\"Description\",\"defaultValue\":\"defaultValue\",\"type\":\"STRING\",\"propertyScopedValues\":[{\"value\":\"value1\",\"scopeSet\":[{\"key\":\"scope-key-1\",\"value\":\"scope-value-1\"}]}]}";
		
		final PropertyScopedValue propertyScopedValue = PropertyScopedValue.builder(ScopeUtils.parseScopeString("scope-key-1=scope-value-1"), "value1").build();
		
		final Property property = new Property.Builder("propertyName")
								.withDescription("Description")
								.withDefaultValue("defaultValue")
								.withScopedValues(propertyScopedValue)
								.withType(PropertyType.STRING)
								.build();
		final String json = jsonUtils.toJson(property);
		
		assertEquals(expected, json);
	}
	
	@Test
	public void fromJsonTest() throws IOException {
		
	    final String json = "{\"name\":\"propertyName\",\"description\":\"Description\",\"defaultValue\":\"defaultValue\",\"propertyScopedValues\":[{\"value\":\"value1\",\"scopeSet\":[{\"key\":\"scope-key-1\",\"value\":\"scope-value-1\"}]}]}";
		
		final Property property = jsonUtils.fromJson(json, Property.class);
		assertNotNull(property);
		assertEquals("propertyName", property.getName());
		assertEquals("Description", property.getDescription());
	}
	
	@Configuration
	@ComponentScan("com.kenzan.henge")
	public static class TestConfig {
	    
	}
	
}
	