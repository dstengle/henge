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
package com.kenzan.henge.resource.v1;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.domain.validator.CheckModelReference;
import com.kenzan.henge.exception.HengeException;
import com.kenzan.henge.service.VersionSetMappingBD;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapping REST Service. This class provides endpoints to add new mapping entries and list the existing ones.
 * 
 * @author wmatsushita
 */
@Component
@Api("v1 - mapping")
@Path("/v1/mapping")
public class MappingRS {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MappingRS.class);
	
	private VersionSetMappingBD mappingBD;
	
	private JsonUtils jsonUtils;
	
	
    @Autowired
    public MappingRS(VersionSetMappingBD mappingBD, JsonUtils jsonUtils) {
        this.mappingBD = mappingBD;
        this.jsonUtils = jsonUtils;
    }

    /**
     * Creates a new entry or updates an existing one in the {@link Mapping} of {@link Scope}s to {@link VersionSetReference}s. 
     * 
     * @param application the name of the application. It's optional.
     * @param scopeString the comma separated list of scopes (key=value) that defines the scopeSet.
     * @param versionSetReference the {@link VersionSetReference} to be mapped to the given parameters.
     */
    @ApiOperation(value = "Creates a new entry or updates an existing one in the Mapping of Scopes to VersionSetReferences.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 203, message = "SUCCESS", response = Response.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "setMapping")
    @PUT
    public Response setMapping(
            @QueryParam(value="application") @ApiParam("application") final String application,
            @QueryParam(value="scopeString") @ApiParam("scopeString") final String scopeString,
            @Valid @CheckModelReference(VersionSet.class) final VersionSetReference versionSetReference) {
    
        LOGGER.info("MappingRS :: setMapping Start : application [{}], scopeString[{}], versionSetReference: \n{}", application, scopeString, versionSetReference);
        
        try {
            
            final Set<Scope> scopeSet =
                            StringUtils.isNotBlank(scopeString) ? ScopeUtils.parseScopeString(scopeString) : Sets.newHashSet();
            
            mappingBD.setMapping(Optional.ofNullable(application), scopeSet, versionSetReference);
            
            LOGGER.info("MappingRS :: setMapping End : application [{}], scopeString[{}], versionSetReference: \n{}", application, scopeString, versionSetReference);
			
            return Response.ok().entity(jsonUtils.toIndentedJson(mappingBD.getAllMappings())).build();
        } catch(JsonProcessingException e) {
            LOGGER.error("MappingRS:: setMapping Error:: application [{}], scopeString[{}], versionSetReference: \n{}", application, scopeString, versionSetReference, e);
			throw new HengeException(Status.INTERNAL_SERVER_ERROR, "Problem serializing the mappings to JSON", e);
        }
        
    }
    
    /**
	 * Retrieves the list of all the {@link Mapping} entries.
	 * 
	 * @return all the {@link Mapping} entries.
	 */
	@ApiOperation(value = "Retrieves the list of all the Mapping entries.", response = Response.class)
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = Response.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "readAll")
	@GET
	public Response readAllMappings() {
       
        try {
        	
			return Response.ok().entity(jsonUtils.toIndentedJson(mappingBD.getAllMappings())).build();
			
		} catch (JsonProcessingException e) {
			LOGGER.error("MappingRS :: readAllMappgins Error :: msg[{}], cause[{}]", e.getMessage(), e.getCause());
			
			throw new HengeException(Status.INTERNAL_SERVER_ERROR, "Problem serializing the mappings to JSON", e);
		}
        
	}
	
    /**
     * Removes the entry from the {@link Mapping} of {@link Scope}s to {@link VersionSetReference}s. 
     * 
     * @param application the name of the application. It's optional.
     * @param scopeString the comma separated list of scopes (key=value) that defines the scopeSet.
     */
    @ApiOperation(value = "Removes the entry from the Mapping of Scopes to VersionSetReferences.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 203, message = "SUCCESS", response = Response.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "delete")
    @DELETE
    public Response deleteMapping(
            @QueryParam(value="application") @ApiParam("application") final String application,
            @QueryParam(value="scopeString") @ApiParam("scopeString") final String scopeString) {
    
        LOGGER.info("MappingRS :: deleteMapping Start : application [{}], scopeString[{}]", application, scopeString);
        
        try {
            
            final Set<Scope> scopeSet =
                            StringUtils.isNotBlank(scopeString) ? ScopeUtils.parseScopeString(scopeString) : Sets.newHashSet();
            
            mappingBD.deleteMapping(Optional.ofNullable(application), scopeSet);
            
            LOGGER.info("MappingRS :: deleteMapping End : application [{}], scopeString[{}]", application, scopeString);
			
            return Response.ok().entity(jsonUtils.toIndentedJson(mappingBD.getAllMappings())).build();    
            
        } catch(JsonProcessingException e) {
            LOGGER.error("MappingRS :: deleteMapping Error:: application [{}], scopeString[{}]", application, scopeString, e);
			throw new HengeException(Status.INTERNAL_SERVER_ERROR, "Problem serializing the mappings to JSON", e);
        }
        
    }	

}
