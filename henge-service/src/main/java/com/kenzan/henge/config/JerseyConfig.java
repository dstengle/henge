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
package com.kenzan.henge.config;

import javax.annotation.PostConstruct;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.kenzan.henge.resource.v1.ConvenienceRS;
import com.kenzan.henge.resource.v1.FileRS;
import com.kenzan.henge.resource.v1.MappingRS;
import com.kenzan.henge.resource.v1.PropertyGroupRS;
import com.kenzan.henge.resource.v1.SearchRS;
import com.kenzan.henge.resource.v1.VersionSetRS;

import io.swagger.jaxrs.config.BeanConfig;

@Configuration
@ConfigurationProperties
public class JerseyConfig extends ResourceConfig {
	
	@Value("${swagger.api.version}")
	private String apiVersion;
	
	@Value("${swagger.schemes}")
	private String schemes;
	
	@Value("${swagger.domain}")
	private String domain;
	
	@Value("${swagger.port}")
	private String port;
	
	@Value("${swagger.base.path}")
	private String basePath;
	
	@Value("${swagger.resource.package}")
	private String resourcePackage;
	
	@Value("${swagger.scan}")
	private String swaggerScan;
	
	@Autowired
	private JerseyJacksonObjectMapperConfig jerseyJacksonConfig; 

	@PostConstruct
	public void init() {
		this.registerSwagger();
		
		// Register WADL resource -> Provides a WADL for the registered REST resources
		register(WadlResource.class);
		register(VersionSetRS.class);
		register(PropertyGroupRS.class);
		register(SearchRS.class);
		register(FileRS.class);
		register(MappingRS.class);
		register(LoggingFilter.class);
		register(ConvenienceRS.class);
		//here is how we hook our jackson object mapper into jersey
		register(jerseyJacksonConfig);
		register(MultiPartFeature.class);

		property(ServletProperties.FILTER_FORWARD_ON_404, true);
		
		// Configure JSR-303 bean validation with Jersey 2.x
		property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
        
        packages(true, "com.jersey.resources", "com.kenzan.henge.exception.mapper");
	}

	private void registerSwagger() {
		
		String host = (!port.isEmpty()) ? this.domain+":"+this.port : this.domain;

		// Swagger Configuration
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion(this.apiVersion);
		beanConfig.setSchemes(new String[] { this.schemes });
		beanConfig.setHost(host);
		beanConfig.setBasePath(this.basePath);
		beanConfig.setResourcePackage(this.resourcePackage);
		beanConfig.setScan(Boolean.valueOf(this.swaggerScan));

		// Swagger Resources
		register(io.swagger.jaxrs.listing.ApiListingResource.class);
		register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
	}
	
}
