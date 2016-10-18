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
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.service.PropertyGroupBD;

import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * {@link PropertyGroupRS} resource.
 *
 * A {@link PropertyGroup} has a name, description, type and a {@link Set} of associated {@link Property}(s).
 * 
 * @author kylebober
 *
 */
@Component
@Produces("application/json")
@Consumes("application/json")
@Path("/v1/property-groups")
@Api("v1 - property-groups")
public class PropertyGroupRS {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyGroupRS.class);
	
	// Set this as a dynamic property via a flatfile
	@Value("${active.version.keyword.list}")
	private static String ACTIVE_VERSION_KEYWORDS;
	
	private PropertyGroupBD propertyGroupBD;
	
    @Autowired
    public PropertyGroupRS(PropertyGroupBD propertyGroupBD) {
        this.propertyGroupBD = propertyGroupBD;
    }

	/**
	 * Create a new {@link PropertyGroup}. 
	 *  
	 * @param entity the {@link PropertyGroup} to be created.
	 * @return the {@link PropertyGroup} created with a unique name
	 */
	@ApiOperation(value = "Create a PropertyGroup.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")    
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = PropertyGroup.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "create")
	@POST
	public PropertyGroup create(@Valid final PropertyGroup entity) {
		
		LOGGER.info("PropertyGroupRS :: Create Start : \nEntity :\n{}", entity);
		
		PropertyGroup createdEntity = propertyGroupBD.create(entity);
		LOGGER.info("PropertyGroupRS :: Create End : \nEntity :\n{}", createdEntity);		

		return createdEntity;		
		
	}	
	
	/**
	 * Update a {@link PropertyGroup}
	 * 
	 * @param propertyGroupName the name of the {@link PropertyGroup} receiving the update 
	 * @param entity {@link PropertyGroup} entity to update by property-group.name
	 */
	@ApiOperation(value = "Update a PropertyGroup by name.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 203, message = "SUCCESS", response = PropertyGroup.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "update")
	@PUT
	@Path("/{propertyGroupName}")
	public PropertyGroup update(
			@PathParam(value="propertyGroupName") @ApiParam("propertyGroupName") final String propertyGroupName, 
			@Valid final PropertyGroup entity) {
	
		LOGGER.info("PropertyGroupRS :: Update Start : PropertyGroupName [{}] \n{}", propertyGroupName, entity);
		
		Optional<PropertyGroup> propertyGroup = propertyGroupBD.update(propertyGroupName, entity);
        if(!propertyGroup.isPresent()) {
            throw new HengeResourceNotFoundException("No PropertyGroup was found by the given name ["+propertyGroupName+"] to be updated. Consider creating a new one.");
        }

		LOGGER.info("PropertyGroupRS :: Update End : PropertyGroupName [{}]", propertyGroupName);       
        return propertyGroup.get();
		
	}
	
    /**
     * Deletes all the versions of a {@link PropertyGroup} by the given name.
     * 
     * @param propertyGroupName the name of the {@link PropertyGroup} to delete
     */
    @ApiOperation(value = "Delete a PropertyGroup by name.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "SUCCESS"),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "deleteAllVersions")
    @DELETE 
    @Path("/{propertyGroupName}")
    public PropertyGroup delete(
            @PathParam(value="propertyGroupName") @ApiParam(value="propertyGroupName") final String propertyGroupName) {
        
        LOGGER.info("PropertyGroupRS :: Delete Start : PropertyGroupName [{}]", propertyGroupName);
        
        Optional<PropertyGroup> entity = propertyGroupBD.delete(propertyGroupName);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No PropertyGroup was found by the given name ["+propertyGroupName+"] to be deleted.");
        }
            
        LOGGER.info("PropertyGroupRS :: Delete End : PropertyGroupName [{}]", propertyGroupName);
        return entity.get();
        
    }

    /**
	 * Delete a {@link PropertyGroup} by name and version
	 * 
	 * @param propertyGroupName the name of the {@link PropertyGroup} to delete
	 * @param propertyGroupVersion the version of the {@link PropertyGroup} to delete
	 */
	@ApiOperation(value = "Delete a PropertyGroup by name and version.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "SUCCESS"),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "deleteSpecificVersion")
	@DELETE 
	@Path("/{propertyGroupName}/versions/{propertyGroupVersion}")
	public PropertyGroup delete(
			@PathParam(value="propertyGroupName") @ApiParam(value="propertyGroupName") final String propertyGroupName,
			@PathParam(value="propertyGroupVersion") @ApiParam(value="propertyGroupVersion") final String propertyGroupVersion) {
		
		LOGGER.info("PropertyGroupRS :: Delete Start : PropertyGroupName [{}], PropertyGroupVersion [{}]", propertyGroupName, propertyGroupVersion);
		
		Optional<PropertyGroup> entity = propertyGroupBD.delete(propertyGroupName, propertyGroupVersion);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No PropertyGroup was found by the given name ["+propertyGroupName+"] and version ["+propertyGroupVersion+"] to be deleted.");
        }
			
        LOGGER.info("PropertyGroupRS :: Delete End : PropertyGroupName [{}], PropertyGroupVersion [{}]", propertyGroupName, propertyGroupVersion);
		return entity.get();
		
	}
	
	/**
	 * Retrieve a {@link PropertyGroup} by name and version.
	 * 
	 * @param propertyGroupName the name of the {@link PropertyGroup} to retrieve
	 * @param propertyGroupVersion the version of the {@link PropertyGroup} to retrieve
	 * @return the {@link PropertyGroup} associated with the name and version
	 */
	@ApiOperation(value = "Get a PropertyGroup by name and version. ")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = PropertyGroup.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "readSpecificVersion")
	@GET
	@Path("/{propertyGroupName}/versions/{propertyGroupVersion}")
	public PropertyGroup read(
			@PathParam(value="propertyGroupName") @ApiParam("propertyGroupName") final String propertyGroupName,
			@PathParam(value="propertyGroupVersion") @ApiParam("propertyGroupName") final String propertyGroupVersion) {
		
		LOGGER.info("PropertyGroupRS :: Read Start : PropertyGroupName [{}], PropertyGroupVersion[{}]", propertyGroupName, propertyGroupVersion);
		
		Optional<PropertyGroup> entity = propertyGroupBD.read(propertyGroupName, propertyGroupVersion);
		if(!entity.isPresent()) {
		    throw new HengeResourceNotFoundException("No PropertyGroup was found by the given name ["+propertyGroupName+"] and version ["+propertyGroupVersion+"].");
		}
					
        LOGGER.info("PropertyGroupRS :: Read End : PropertyGroupName [{}], PropertyGroupVersion[{}] \nEntity :\n{}", propertyGroupName, propertyGroupVersion, entity);      
        return entity.get();
	}
	
    /**
     * Retrieves the latest version of a {@link PropertyGroup} by name.
     * 
     * @param propertyGroupName the name of the {@link PropertyGroup} to retrieve
     * @return the latest version of the {@link PropertyGroup} associated with the given name
     */
    @ApiOperation(value = "Get the latest PropertyGroup.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "SUCCESS", response = PropertyGroup.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "readLatestVersion")
    @GET
    @Path("/{propertyGroupName}/versions/latest")
    public PropertyGroup readLatest(
            @PathParam(value="propertyGroupName") @ApiParam("propertyGroupName") final String propertyGroupName) {
        
        LOGGER.info("PropertyGroupRS :: Read Start : PropertyGroupName [{}]", propertyGroupName);
        
        Optional<PropertyGroup> entity = propertyGroupBD.read(propertyGroupName);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No PropertyGroup was found by the given name ["+propertyGroupName+"]");
        }
            
        LOGGER.info("PropertyGroupRS :: Read End : PropertyGroupName [{}], \nEntity :\n{}", propertyGroupName, entity);      
        return entity.get();
    }

	/**
	 * Retrieve the latest {@link PropertyGroup} version number by name.
	 * 
	 * @param propertyGroupName the name of the {@link PropertyGroup} to retrieve
	 * @return the {@link PropertyGroup} associated with the name and latest version
	 */
	@ApiOperation(value = "Gets the latest version number of a PropertyGroup by name. ")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="plain/text", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = PropertyGroup.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "versionCeiling")
	@Produces("plain/text")
	@GET
	@Path("/{propertyGroupName}/versions/ceiling")
	public String readLatestVersionNumber(
			@PathParam(value="propertyGroupName") @ApiParam("propertyGroupName") final String propertyGroupName) {
		
		LOGGER.info("PropertyGroupRS :: Read Latest Version Number Start : PropertyGroupName [{}]", propertyGroupName);
		Optional<PropertyGroup> entity = propertyGroupBD.read(propertyGroupName);
			
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No PropertyGroup was found by the given name ["+propertyGroupName+"].");
        }
            
        LOGGER.info("PropertyGroupRS :: Read Latest Version Number End : PropertyGroupName [{}], \nEntity :\n{}", propertyGroupName, entity);     
        return entity.get().getVersion();
		
	}
	
	/**
	 * Return a set of version numbers associated with the PropertyGroup based on PropertyGroupName
	 * @param propertyGroupName
	 * @return a set of {@link String} containing the existing versions of {@link PropertyGroup} with the given name
	 */
	@ApiOperation(value = "Return a set of version numbers associated with a PropertyGroup by name.", response = PropertyGroup.class)
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = PropertyGroup.class, responseContainer = "Set"),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "versions")
	@GET
	@Path(value="/{propertyGroupName}/versions")
	public Set<String> versions(@PathParam("propertyGroupName") @ApiParam(value="propertyGroupName") final String propertyGroupName) {

	    LOGGER.info("PropertyGroupRS :: Versions Start : PropertyGroupName [{}]", propertyGroupName);
		
	    Optional<Set<String>> versions = propertyGroupBD.versions(propertyGroupName);
        if(!versions.isPresent()) {
            throw new HengeResourceNotFoundException("No PropertyGroup was found by the given name ["+propertyGroupName+"].");
        }
	    
        LOGGER.info("PropertyGroupRS :: Versions End : PropertyGroupName [{}]", propertyGroupName);     
	    return versions.get();
	    
	}
	
}
