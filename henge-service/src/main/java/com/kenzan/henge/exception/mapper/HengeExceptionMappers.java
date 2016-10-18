package com.kenzan.henge.exception.mapper;

import static com.google.common.base.Preconditions.checkNotNull;

import com.kenzan.henge.exception.HengeAuthenticationException;
import com.kenzan.henge.exception.HengeAuthorizationException;
import com.kenzan.henge.exception.HengeException;
import com.kenzan.henge.exception.HengeIOException;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.exception.HengeValidationException;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maps exepctions to HTTP response codes.
 *
 * @author wmatsushita
 */
public class HengeExceptionMappers {

private static final Logger LOGGER = LoggerFactory.getLogger(HengeExceptionMappers.class);
    
    @Provider
    @Singleton
    public static class HengeExceptionMapper implements ExceptionMapper<HengeException> {

        @Override
        public Response toResponse(HengeException exception) {
            
            checkNotNull(exception);
            
            LOGGER.error(exception.getMessage());
            return Response.status(exception.getStatus()).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
    }
    
    @Provider
    @Singleton
    public static class HengeValidationExceptionMapper implements ExceptionMapper<HengeValidationException> {

        @Override
        public Response toResponse(HengeValidationException exception) {
            
            checkNotNull(exception);
            
            LOGGER.error(exception.getMessage());
            return Response.status(exception.getStatus()).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
    }
    
    @Provider
    @Singleton
    public static class HengeIOExceptionMapper implements ExceptionMapper<HengeIOException> {

        @Override
        public Response toResponse(HengeIOException exception) {
            
            checkNotNull(exception);
            
            LOGGER.error(exception.getMessage());
            return Response.status(exception.getStatus()).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
    }
    
    @Provider
    @Singleton
    public static class HengeAuthenticationExceptionMapper implements ExceptionMapper<HengeAuthenticationException> {

        @Override
        public Response toResponse(HengeAuthenticationException exception) {
            
            checkNotNull(exception);
            
            LOGGER.error(exception.getMessage());
            return Response.status(HttpStatus.SC_FORBIDDEN).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
    }
    
    @Provider
    @Singleton
    public static class HengeAuthorizationExceptionMapper implements ExceptionMapper<HengeAuthorizationException> {

        @Override
        public Response toResponse(HengeAuthorizationException exception) {
            
            checkNotNull(exception);
            
            LOGGER.error(exception.getMessage());
            return Response.status(HttpStatus.SC_FORBIDDEN).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
    }
    
    @Provider
    @Singleton
    public static class HengeResourceNotFoundExceptionMapper implements ExceptionMapper<HengeResourceNotFoundException> {

        @Override
        public Response toResponse(HengeResourceNotFoundException exception) {
            
            checkNotNull(exception);
            
            LOGGER.error(exception.getMessage());
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
    }
    
}
