package com.kenzan.henge.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kenzan.henge.domain.model.MappingKey;

@Configuration
public class MappingKeyDeserializer extends KeyDeserializer {

	@Override
	public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		return mapper.readValue(key, MappingKey.class);
		
	}

}
