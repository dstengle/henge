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
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.validator.CheckVersionSet;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.service.VersionSetBD;

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
import org.springframework.stereotype.Component;


/**
 * {@link VersionSet} resource.
 * 
 * 	A {@link VersionSet} comprises of a set of {@link PropertyGroup}s. 
 * 	{@link PropertyGroup} must be unique, i.e. No duplicate {@link PropertyGroup} names.
 * 
 * 	A {@link VersionSet} has a set of {@link PropertyGroup}s that contain {@link Property} with PropertyScopedValues associated with a query Set of {@link PropertyScopedValue}, i.e., environment=dev&stack=sbnet;env=qa&stack=sbnet;env=stage&stack=sbnet;env=prod&stack=sbnet
 * 	
 * @author kylebober
 *
 */
@Component
@Produces("application/json")
@Consumes("application/json")
@Path("/v1/version-sets")
@Api("v1 - version-sets")
public class VersionSetRS {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetRS.class);

	private VersionSetBD versionSetBD;
	
	
	@Autowired
	public VersionSetRS(VersionSetBD versionSetBD) {
	    this.versionSetBD = versionSetBD;
	}
	

	/**
	 * Create a new {@link VersionSet}. 
	 *  
	 * @param entity the {@link VersionSet} to be created.
	 * @return the {@link VersionSet} created
	 */
	@ApiOperation(value = "Create a VersionSet.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")    
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = VersionSet.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "create")
	@POST
	public VersionSet create(@Valid @CheckVersionSet final VersionSet entity) {
		
		LOGGER.info("VersionSetRS :: Create Start : \nEntity :\n{}", entity);		
		
		VersionSet createdEntity = versionSetBD.create(entity);
		LOGGER.info("VersionSetRS :: Create End : \nEntity :\n{}", createdEntity);		

		return createdEntity;
		
	}	
	
	/**
	 * Update a {@link VersionSet}
	 * 
	 * @param name the name of the {@link VersionSet} that is receiving the update
	 * @param entity {@link VersionSet} entity to update at the provided name
	 */
	@ApiOperation(value = "Update a VersionSet.", response = VersionSet.class)
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 203, message = "SUCCESS"),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "update")
	@PUT
	@Path("/{versionSetName}")
	public VersionSet update(
			@PathParam(value="versionSetName") @ApiParam("versionSetName") final String name, 
			@Valid @CheckVersionSet final VersionSet entity) {
		
		LOGGER.info("VesionSetRS :: Update Start : VersionSetName [{}] \n{}", name, entity);		
		
		Optional<VersionSet> versionSet = versionSetBD.update(name, entity);
		if(!versionSet.isPresent()) {
		    throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+name+"].");
		}
		
        LOGGER.info("VersionSetRS :: Update End : VersionSetName [{}]", name);
        return versionSet.get();
	}
	
	/**
	 * Deletes all the versions of a {@link VersionSet} by name
	 * 
	 * @param name the name of the {@link VersionSet} to delete
	 */
	@ApiOperation(value = "Delete a VersionSet.", response = VersionSet.class)
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "SUCCESS", response = VersionSet.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "deleteSpecificVersion")
	@DELETE
	@Path("/{versionSetName}")
	public VersionSet delete(@PathParam(value="versionSetName") @ApiParam("versionSetName") final String name) {
		
		LOGGER.info("VesionSetRS :: Delete Start : VersionSetName [{}]", name);
		
		Optional<VersionSet> entity = versionSetBD.delete(name);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+name+"].");
        }
				
		LOGGER.info("VesionSetRS :: Delete Start : VersionSetName [{}]", name);
		return entity.get();
	}
	
    /**
     * Deletes a {@link VersionSet} by name and version
     * 
     * @param versionSetName the name of the {@link VersionSet} to delete
     * @param versionSetVersion the version of the {@link VersionSet} to delete
     */
    @ApiOperation(value = "Delete a VersionSet by name.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "SUCCESS", response = VersionSet.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "deleteAllVersions")
    @DELETE 
    @Path("/{versionSetName}/versions/{versionSetVersion}")
    public VersionSet delete(
            @PathParam(value="versionSetName") @ApiParam(value="versionSetName") final String versionSetName,
            @PathParam(value="versionSetVersion") @ApiParam(value="versionSetVersion") final String versionSetVersion) {
        
        LOGGER.info("VersionSetRS :: Delete Start : VersionSetName [{}], VersionSetVersion [{}]", versionSetName, versionSetVersion);
        
        Optional<VersionSet> entity = versionSetBD.delete(versionSetName, versionSetVersion);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+versionSetName+"] and version ["+versionSetVersion+"] to be deleted.");
        }
            
        LOGGER.info("VersionSetRS :: Delete End : VersionSetName [{}], VersionSetVersion [{}]", versionSetName, versionSetVersion);
        return entity.get();
        
    }

    /**
	 * Retrieves the latest version of a {@link VersionSet} by name.
	 * 
	 * @param name the name of the {@link VersionSet} to retrieve
	 * @return the {@link VersionSet} associated with the name
	 */
	@ApiOperation(value = "Read the latest version of a VersionSet by name.", response = VersionSet.class)
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = VersionSet.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "readLatestVersion")
	@GET
	@Path("/{versionSetName}")
	public VersionSet read(@PathParam(value="versionSetName") @ApiParam("versionSetName") final String name) {
		
		LOGGER.info("VesionSetRS :: Read Start : VersionSetName [{}]", name);
		
		Optional<VersionSet> entity = versionSetBD.read(name);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+name+"].");
        }
			
        LOGGER.info("VesionSetRS :: Read End : VersionSetName [{}] \nEntity :\n{}", name, entity);
        return entity.get();
        
	}
	
    /**
     * Retrieve a {@link VersionSet} by name and version.
     * 
     * @param versionSetName the name of the {@link VersionSet} to retrieve
     * @param versionSetVersion the version of the {@link VersionSet} to retrieve
     * @return the {@link VersionSet} associated with the name and version
     */
    @ApiOperation(value = "Get a VersionSet by name and version. ")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "SUCCESS", response = VersionSet.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "readSpecificVersion")
    @GET
    @Path("/{versionSetName}/versions/{versionSetVersion}")
    public VersionSet read(
            @PathParam(value="versionSetName") @ApiParam("versionSetName") final String versionSetName,
            @PathParam(value="versionSetVersion") @ApiParam("versionSetName") final String versionSetVersion) {
        
        LOGGER.info("VersionSetRS :: Read Start : VersionSetName [{}], VersionSetVersion[{}]", versionSetName, versionSetVersion);
        
        Optional<VersionSet> entity = versionSetBD.read(versionSetName, versionSetVersion);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+versionSetName+"] and version ["+versionSetVersion+"].");
        }
            
        
        LOGGER.info("VersionSetRS :: Read End : VersionSetName [{}], VersionSetVersion[{}] \nEntity :\n{}", versionSetName, versionSetVersion, entity);      
        return entity.get();
    }
    
    /**
	 * Retrieves the latest version number of a {@link VersionSet} by name.
	 * 
	 * @param name the name of the {@link VersionSet} to retrieve
	 * @return the version associated with last {@link VersionSet} found for name provided
	 */
	@ApiOperation(value = "Read the latest version number of a VersionSet by name.", response = VersionSet.class)
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="plain/text", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = VersionSet.class),
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
	@Path("/{versionSetName}/versions/ceiling")
	public String readLatestVersionNumber(@PathParam(value="versionSetName") @ApiParam("versionSetName") final String name) {
		
		LOGGER.info("VesionSetRS :: Read Latest Version Number Start : VersionSetName [{}]", name);
		
		Optional<VersionSet> entity = versionSetBD.read(name);
        if(!entity.isPresent()) {
            throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+name+"].");
        }
			
        LOGGER.info("VesionSetRS :: Read End : VersionSetName [{}] \nEntity :\n{}", name, entity);
        return entity.get().getVersion();
        
	}
    
    /**
     * Return a set of version numbers associated with the {@link VersionSet} based on VersionSetName
     * @param versionSetName
     * @return a set of {@link String} containing the existing versions of {@link VersionSet} with the given name
     */
    @ApiOperation(value = "Return a set of version numbers associated with a VersionSet by name.", response = VersionSet.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "SUCCESS", response = VersionSet.class, responseContainer = "Set"),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "versions")
    @GET
    @Path(value="/{versionSetName}/versions")
    public Set<String> versions(@PathParam("versionSetName") @ApiParam(value="versionSetName") final String versionSetName) {

        LOGGER.info("VersionSetRS :: Versions Start : VersionSetName [{}]", versionSetName);
        
        Optional<Set<String>> versions = versionSetBD.versions(versionSetName);
        if(!versions.isPresent()) {
            throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+versionSetName+"].");
        }
        
        LOGGER.info("VersionSetRS :: Versions End : VersionSetName [{}]", versionSetName);     
        return versions.get();
        
    }    

}
