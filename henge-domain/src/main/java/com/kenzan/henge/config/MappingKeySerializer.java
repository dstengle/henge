package com.kenzan.henge.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.kenzan.henge.domain.model.MappingKey;

@Configuration
public class MappingKeySerializer extends StdSerializer<MappingKey> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected MappingKeySerializer() {
		super(MappingKey.class);
	}
	
	@Override
	public void serialize(MappingKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		gen.writeFieldName(mapper.writeValueAsString(value));
		
	}

}
