package com.kenzan.hello.properties.config;

import com.kenzan.hello.properties.rs.PropertyManagerRS;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class RestModule extends JerseyServletModule {

	@Override
	protected void configureServlets() {
		bind(PropertyManagerRS.class);
		
		serve("/*").with(GuiceContainer.class);
	}
	
}
