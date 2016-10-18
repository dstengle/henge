package com.kenzan.henge.domain.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.io.ByteStreams;
import com.kenzan.henge.exception.RuntimeHengeException;

@Component
public class JsonUtils {
	
	private ObjectMapper mapper;
	
	@Autowired
	public JsonUtils(ObjectMapper mapper) {
	    this.mapper = mapper;
	}
	
	public <T> T fromJson(final String json, final Class<T> clazz) throws IOException {
		
		return fromJson(json, clazz, false);
	}
	
	public <T> T fromJson(final String json, final TypeReference<T> type) throws IOException {
		
		return new ObjectMapper().readValue(json, type);
	}
	
	public <T> T fromJson(final String json, final Class<T> clazz, final boolean convertUnderscore) throws IOException {
		JacksonJaxbJsonProvider parser = new JacksonJaxbJsonProvider();
        parser.setMapper(mapper);
        
        if (convertUnderscore) {
        	//TODO: improve this to inject another mapper instead of creating a new one every time.
            ObjectMapper newMapper = new ObjectMapper();
            newMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            parser.setMapper(newMapper);
        }
        
        ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes());
        @SuppressWarnings("unchecked") T result =
            (T) parser.readFrom(Object.class, clazz, null, MediaType.APPLICATION_JSON_TYPE, null, is);
        return result;
    }

    public <T> T fromJson(final InputStream json, final Class<T> clazz) throws IOException {

        return fromJson(toString(json), clazz);
    }

    public String toJson(final Object instance) throws JsonProcessingException {

        return mapper.writeValueAsString(instance);
    }
    
    public String toIndentedJson(final Object instance) throws JsonProcessingException {

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }
    
	public List<String> toJson(final List<?> jsons) {
		
		return jsons.stream().map(json -> {
			try {
				return toJson(json);
			} catch (JsonProcessingException e) {
				throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,e.getMessage(),e);
			}
		}).collect(Collectors.toList());
    }
    
    public String toString(final InputStream is) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(is, out);
        return out.toString();
    }

}
