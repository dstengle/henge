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
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.service.SearchBD;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TBD if we are even going to need this Resource. Each of the entity resources have there own search resource endpoint
 * @author kylebober
 * @author Igor K. Shiohara
 * @author wmatsushita
 */
@Component
@Api("v1 - search")
@Path("/v1/search")
public class SearchRS {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchRS.class);
	
	private SearchBD searchBD;
	
	
    @Autowired
    public SearchRS(SearchBD searchBD) {
        this.searchBD = searchBD;
    }

	@ApiOperation(value = "Gets the Application properties and internal libraries properties within given precedence scopes. ")
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
	@Timed(name = "search")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{application}")
	public Response findMatches(@PathParam(value="application") @ApiParam("application") final String application, 
								@QueryParam(value = "scopes") @ApiParam("scopes") final String scopes,
                                @QueryParam(value = "libs") @ApiParam("libs") final String libs) {
		try {
		    
			Optional<String> properties = searchBD.findProperties(application, scopes, Optional.ofNullable(libs));
			
			if(!properties.isPresent()) {
                throw new HengeResourceNotFoundException("No Properties were found by the given application ["+application+"] and scopeString [" + scopes + "].");
            }
			
			return Response.ok().entity(properties.get()).type(MediaType.TEXT_PLAIN).build();
			
		} catch (Exception e) {
			LOGGER.error("VersionSetRS :: findMatches ERROR : \nApplication \n{}", application, e);
			throw e;
		}
	}
	
}
