package com.kenzan.henge.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.ext.ContextResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author wmatsushita
 */
@Configuration
public class JerseyJacksonObjectMapperConfig implements ContextResolver<ObjectMapper> {
	
	@Autowired
	private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getContext(final Class<?> type) {

        return objectMapper;
        
    }

}
