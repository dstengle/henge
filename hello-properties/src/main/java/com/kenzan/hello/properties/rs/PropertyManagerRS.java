package com.kenzan.hello.properties.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

@Path("/v1/")
@Produces(MediaType.TEXT_PLAIN)
public class PropertyManagerRS {
	
	@GET
	@Path("/sayHello/{propertyKey}")
	public Response sayHello(@PathParam("propertyKey") String propertyKey) {
		DynamicStringProperty name = DynamicPropertyFactory.getInstance().getStringProperty(propertyKey, "");
		return Response.ok("Hello " + name.get()).build();
	}
	
	@GET
	@Path("/displayMessage/{propertyKey}")
	public Response displayMessage(@PathParam("propertyKey") String propertyKey) {
		DynamicStringProperty message = DynamicPropertyFactory.getInstance().getStringProperty(propertyKey, "");
		DynamicStringProperty name = DynamicPropertyFactory.getInstance().getStringProperty("helloproperties.name", "");
		return Response.ok(name.get() + " says: " + message.get()).build();
	}
	
}
