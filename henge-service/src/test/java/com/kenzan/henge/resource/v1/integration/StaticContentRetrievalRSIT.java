/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober
 * (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kenzan.henge.resource.v1.integration;

import static com.jayway.restassured.RestAssured.given;

import com.jayway.restassured.RestAssured;
import com.kenzan.henge.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlProcessor.ResolutionMethod;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * 
 *
 * 
 *
 * 
 * @author gclavell
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class , StaticContentRetrievalRSIT.TestContextConfiguration.class})
@WebAppConfiguration
//@ActiveProfiles({ "dev", "flatfile_local", "setmapping" })
@IntegrationTest("server.port=0")

public class StaticContentRetrievalRSIT {

    @Value("${local.server.port}")
    private int port;

    @Before
    public void init() {

        RestAssured.port = port;
    }

    
    @Test
    public void swaggerJSONRetrievalTest() {

    	given().auth().basic("user", "user").expect().statusCode(200).when().get("/henge/swagger.json")
    	.then().assertThat().statusCode(200);
        
    }

   
    @Test
    public void swaggerDocumentationRetrievalTest() {

   
        given().auth().preemptive().basic("user", "user").contentType("application/json").when()
            .get("/henge/swagger/index.html").then().assertThat().statusCode(200);
    }

    @Configuration
    @ComponentScan("com.kenzan.henge")
    public static class TestContextConfiguration {

        @Bean
        public static PropertySourcesPlaceholderConfigurer properties() throws Exception {

            final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();

            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResolutionMethod(ResolutionMethod.OVERRIDE);
            yaml.setResources(new ClassPathResource("application.yml"), new ClassPathResource(
                "application-flatfile_local.yml"));
            pspc.setProperties(yaml.getObject());

            return pspc;
        }

    }
}
