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
package com.kenzan.henge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kenzan.henge.domain.model.MappingKey;

@Configuration
public class JacksonConfig {
	
	@Autowired
	private MappingKeySerializer mappingKeySerializer;
	
	@Autowired
	private MappingKeyDeserializer mappingKeyDeserializer;

	// Add some configuration property parameters here to controller the Jackson ObjectMapper
	@Bean
	public ObjectMapper jacksonObjectMapper() {	
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		
		SimpleModule module = new SimpleModule();
		module.addKeySerializer(MappingKey.class, mappingKeySerializer);
		module.addKeyDeserializer(MappingKey.class, mappingKeyDeserializer);

		builder.modules(new JavaTimeModule(), module);
		builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		
		
		ObjectMapper mapper = builder.build();
		
		return mapper;
	}

}
                            