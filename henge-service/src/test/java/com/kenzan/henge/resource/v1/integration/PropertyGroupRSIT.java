package com.kenzan.henge.resource.v1.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.service.PropertyGroupBD;
import com.kenzan.henge.util.AbstractIntegrationTest;
import com.kenzan.henge.util.CleanerUtils;

/**
 *
 *
 * @author wmatsushita
 * @author Igor K. Shiohara
 */
public class PropertyGroupRSIT extends AbstractIntegrationTest {

	@Autowired
	private PropertyGroupBD service;
	
	@Autowired
	private CleanerUtils cleanerUtils;
	
	/**
	 * Create some {@link PropertyGroup} to use in the Integration Tests
	 * @throws IOException
	 */
	@Before
	public void init() throws IOException {
		String name = "PropertyGroup-Test1";
		String version1 = "1.0.0";
		String version2 = "2.0.1";
		String version3 = "2.4.0";
		if (!service.read(name, version1).isPresent()) {
			service.create(PropertyGroup.builder(name, version1)
					.withDescription("PropertyGroup App description")
					.withCreatedBy("Igor K. Shiohara")
					.withIsActive(true)
					.withType(PropertyGroupType.APP.toString())
				.build());
		}
		if (!service.read(name, version2).isPresent()) {
			service.update(name, PropertyGroup.builder(name, version2)
					.withDescription("PropertyGroup App description")
					.withCreatedBy("Igor K. Shiohara")
					.withIsActive(true)
					.withType(PropertyGroupType.APP.toString())
				.build());
		}
		if (!service.read(name, version3).isPresent()) {
			service.update(name, PropertyGroup.builder(name, version3)
					.withDescription("PropertyGroup App description")
					.withCreatedBy("Igor K. Shiohara")
					.withIsActive(true)
					.withType(PropertyGroupType.APP.toString())
				.build());
		}
	}
	
	/**
	 * Tests the creation of a new {@link PropertyGroup} endpoint 
	 */
	@Test
	public void createPropertyGroupTestSucessful() {
		String jsonRequest = "{ "+
				  " \"name\" : \"PropertyGroup-TestCreate\", "+ 
				  " \"version\" : \"1.0.1\", "+
				  " \"description\" : \"property-group-1-description\"," +
				  " \"type\" : \"APP\"," +
				  " \"active\" : true," +
				  " \"createdDate\" : \"2015-11-23T09:44:51.580\"," +
				  " \"properties\" : [ {" +
				  	" \"name\" : \"app-name\"," +
				  	" \"description\" : \"Application Description\"," +
				    " \"defaultValue\" : null," +
				    " \"propertyScopedValues\" : [ {" +
				    	" \"key\" : \"env=dev\"," +
				        " \"value\" : \"Pet Store App - Development\"" +
				    " }, {" +
				    	" \"key\" : \"env=prod\"," +
				    	" \"value\" : \"Pet Store App - Production\"" +
				    	"} ]" +  
				    "}, {" +
				    " \"name\" : \"property-foo\"," +
				    " \"description\" : \"Dummy Description\"," +
				    " \"defaultValue\" : null," +
				    " \"propertyScopedValues\" : [ {" +
				       " \"key\" : \"env=dev\"," +
				       " \"value\" : \"Bar Dev\" " +
				    "}, { " +
				       " \"key\" : \"env=prod\"," + 
				       " \"value\" : \"Bar Prod\" " +
				    "} ] " +
				  "} ] " +
				"}";	
		
		given().auth().basic("user", "user")
			.body(jsonRequest)
			.with().contentType(ContentType.JSON)
			.then().expect()
				.statusCode(200)
				.body("name", equalTo("PropertyGroup-TestCreate"),
					  "version", equalTo("1.0.1"),
					  "description", equalTo("property-group-1-description"),
					  "type", equalTo("APP"),
					  "active", is(true),
					  "createdDate", notNullValue()
					  )
			.when().post("/henge/v1/property-groups");
	}
	
	/**
	 * Tests the updating of an existent {@link PropertyGroup} endpoint
	 */
	@Test
	public void updatePropertyGroupTestSucessful() {
		service.create(PropertyGroup.builder("PropertyGroup-TestUpdate", "1.0.1")
				.withDescription("PropertyGroup App description")
				.withCreatedBy("Igor K. Shiohara")
				.withIsActive(true)
				.withType(PropertyGroupType.APP.toString())
			.build());
		
		String jsonRequest = "{ "+
				  " \"name\" : \"PropertyGroup-TestUpdate\", "+ 
				  " \"version\" : \"1.0.2\", "+
				  " \"description\" : \"property-group-1-description\"," +
				  " \"type\" : \"APP\"," +
				  " \"active\" : true," +
				  " \"createdDate\" : \"2015-11-23T09:44:51.580\"," +
				  " \"properties\" : [ {" +
				  	" \"name\" : \"app-name\"," +
				  	" \"description\" : \"Application Description\"," +
				    " \"defaultValue\" : null," +
				    " \"propertyScopedValues\" : [ {" +
				    	" \"key\" : \"env=dev\"," +
				        " \"value\" : \"Pet Store App - Development\"" +
				    " }, {" +
				    	" \"key\" : \"env=prod\"," +
				    	" \"value\" : \"Pet Store App - Production\"" +
				    	"} ]" +  
				    "}, {" +
				    " \"name\" : \"property-foo\"," +
				    " \"description\" : \"Dummy Description\"," +
				    " \"defaultValue\" : null," +
				    " \"propertyScopedValues\" : [ {" +
				       " \"key\" : \"env=dev\"," +
				       " \"value\" : \"Bar Dev\" " +
				    "}, { " +
				       " \"key\" : \"env=prod\"," + 
				       " \"value\" : \"Bar Prod\" " +
				    "} ] " +
				  "} ] " +
				"}";	
		
		given().auth().basic("user", "user")
			.body(jsonRequest)
			.with().contentType(ContentType.JSON)
			.then().expect()
				.statusCode(200)
				.body("name", equalTo("PropertyGroup-TestUpdate"),
					  "version", equalTo("1.0.2"),
					  "description", equalTo("property-group-1-description"),
					  "type", equalTo("APP"),
					  "active", is(true)
					  )
			.when().put("/henge/v1/property-groups/PropertyGroup-TestUpdate");
	}
	
	/**
     * Tests the updating of a non existent {@link PropertyGroup}
     */
    @Test
    public void updateNotExistPropertyGroupTest() {

        String jsonRequest = "{ "+
                  " \"name\" : \"PropertyGroup-TestUpdate\", "+ 
                  " \"version\" : \"1.0.2\", "+
                  " \"description\" : \"property-group-1-description\"," +
                  " \"type\" : \"APP\"," +
                  " \"active\" : true," +
                  " \"createdDate\" : \"2015-11-23T09:44:51.580\"," +
                  " \"properties\" : [ {" +
                    " \"name\" : \"app-name\"," +
                    " \"description\" : \"Application Description\"," +
                    " \"defaultValue\" : null," +
                    " \"propertyScopedValues\" : [ {" +
                        " \"key\" : \"env=dev\"," +
                        " \"value\" : \"Pet Store App - Development\"" +
                    " }, {" +
                        " \"key\" : \"env=prod\"," +
                        " \"value\" : \"Pet Store App - Production\"" +
                        "} ]" +  
                    "}, {" +
                    " \"name\" : \"property-foo\"," +
                    " \"description\" : \"Dummy Description\"," +
                    " \"defaultValue\" : null," +
                    " \"propertyScopedValues\" : [ {" +
                       " \"key\" : \"env=dev\"," +
                       " \"value\" : \"Bar Dev\" " +
                    "}, { " +
                       " \"key\" : \"env=prod\"," + 
                       " \"value\" : \"Bar Prod\" " +
                    "} ] " +
                  "} ] " +
                "}";    
        
        final Response response = given().auth().basic("user", "user")
            .body(jsonRequest).with().contentType(ContentType.JSON)
            .when().put("/henge/v1/property-groups/PropertyGroup-TestUpdate");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-TestUpdate] to be updated. Consider creating a new one.", response.getBody().asString());
    }
	
	/**
	 * Tests the deletion of a new {@link PropertyGroup} endpoint 
	 * by {@link PropertyGroup} name
	 */
	@Test
	public void deletePropertyGroupByNameTestSuccessful() {
		service.create(PropertyGroup.builder("PropertyGroup-TestDelete", "1.0.1")
				.withDescription("PropertyGroup App description")
				.withCreatedBy("Igor K. Shiohara")
				.withIsActive(true)
				.withType(PropertyGroupType.APP.toString())
			.build());
		
		given().auth().basic("user", "user")
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
			.body("name", equalTo("PropertyGroup-TestDelete"),
				  "version", equalTo("1.0.1"),
				  "description", equalTo("PropertyGroup App description"),
				  "type", equalTo("APP"),
				  "active", is(true)
				  )
		.when().delete("/henge/v1/property-groups/PropertyGroup-TestDelete");
	}
	
	/**
     * Tests the deletion of a non existent {@link PropertyGroup} by name
     * by {@link PropertyGroup} name
     */
    @Test
    public void deleteNonExistentPropertyGrouByName() {
        
        final Response response = given().auth().basic("user", "user")
        .when().delete("/henge/v1/property-groups/PropertyGroup-TestDelete");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-TestDelete] to be deleted.", response.getBody().asString());
    }
    
    /**
     * Tests the deletion of a non existent {@link PropertyGroup} by name
     * by {@link PropertyGroup} name
     */
    @Test
    public void deleteNonExistentPropertyGrouByNameAndVersion() {
        
        final Response response = given().auth().basic("user", "user")
        .when().delete("/henge/v1/property-groups/PropertyGroup-TestDelete2/versions/2.0.1");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-TestDelete2] and version [2.0.1] to be deleted.", response.getBody().asString());
    }
	
	/**
	 * Tests the deletion of a new {@link PropertyGroup} endpoint 
	 * by {@link PropertyGroup} name and version
	 */
	@Test
	public void deletePropertyGroupByNameAndVersionTestSuccessful() {
		service.create(PropertyGroup.builder("PropertyGroup-TestDelete2", "2.0.0")
				.withDescription("PropertyGroup App description")
				.withCreatedBy("Igor K. Shiohara")
				.withIsActive(true)
				.withType(PropertyGroupType.APP.toString())
			.build());
		
		service.update("PropertyGroup-TestDelete2", 
		    PropertyGroup.builder("PropertyGroup-TestDelete2", "2.0.1")
				.withDescription("PropertyGroup App description")
				.withCreatedBy("Igor K. Shiohara")
				.withIsActive(true)
				.withType(PropertyGroupType.APP.toString())
			.build());
		
		given().auth().basic("user", "user")
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
			.body("name", equalTo("PropertyGroup-TestDelete2"),
				  "version", equalTo("2.0.1"),
				  "description", equalTo("PropertyGroup App description"),
				  "type", equalTo("APP"),
				  "active", is(true)
				  )
		.when().delete("/henge/v1/property-groups/PropertyGroup-TestDelete2/versions/2.0.1");
		
		Optional<PropertyGroup> v1 = service.read("PropertyGroup-TestDelete2", "2.0.0");
		assertTrue(v1.isPresent());
		
		Optional<PropertyGroup> v2 = service.read("PropertyGroup-TestDelete2", "2.0.1");
		assertFalse(v2.isPresent());
	}
	
	/**
	 * Tests the read endpoint, to retrieve a {@link PropertyGroup}
	 */
	@Test
	public void readPropertyGroupTestSuccessful() {
		given().auth().basic("user", "user")
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
			.body("name", equalTo("PropertyGroup-Test1"),
				  "version", equalTo("2.0.1"),
				  "description", equalTo("PropertyGroup App description"),
				  "type", equalTo("APP"),
				  "active", is(true)
				  )
		.when().get("/henge/v1/property-groups/PropertyGroup-Test1/versions/2.0.1");
	}
	
	/**
     * Tests the read endpoint, to retrieve the current version {@link PropertyGroup}
     */
    @Test
    public void readCurrentVersionPropertyGroupTestSuccessful() {
        given().auth().basic("user", "user")
        .with().contentType(ContentType.JSON)
        .then().expect()
            .statusCode(200)
            .body("name", equalTo("PropertyGroup-Test1"),
                  "version", equalTo("2.4.0"),
                  "description", equalTo("PropertyGroup App description"),
                  "type", equalTo("APP"),
                  "active", is(true)
                  )
        .when().get("/henge/v1/property-groups/PropertyGroup-Test1/versions/latest");
    }
    
    /**
     * Tests the read endpoint, to a non existent lastest version {@link PropertyGroup}
     */
    @Test
    public void readNonExistentCurrentVersionPropertyGroupTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/property-groups/PropertyGroup-Test5/versions/latest");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-Test5]", response.getBody().asString());
    }
	
	
	/**
     * Tests the read endpoint, to a non existent {@link PropertyGroup}
     */
    @Test
    public void readNonExistentPropertyGroupTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/property-groups/PropertyGroup-Test5/versions/2.0.1");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-Test5] and version [2.0.1].", response.getBody().asString());
    }
    
    /**
     * Tests the retrieving of the latest version number of a specific {@link PropertyGroup}
     */
    @Test
    public void nonExistentLatestVersionNumberTest() {
        
        final Response response = given().auth().basic("user", "user").when().get("/henge/v1/property-groups/PropertyGroup-Test5/versions/ceiling");
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-Test5].", response.getBody().asString());
        
    }
	
	/**
	 * Tests the retrieving of the latest version of a specific {@link PropertyGroup}
	 */
	@Test
	public void latestVersionTest() {
		given().auth().basic("user", "user").when().get("/henge/v1/property-groups/PropertyGroup-Test1/versions/latest")
			.then().statusCode(200).body(
						"name", equalTo("PropertyGroup-Test1"),
						"version", equalTo("2.4.0")
					);
		
	}
	
	/**
	 * Tests the retrieving of the latest version number of a specific {@link PropertyGroup}
	 */
	@Test
	public void latestVersionNumberTest() {
		Response response = given().auth().basic("user", "user").when().get("/henge/v1/property-groups/PropertyGroup-Test1/versions/ceiling");
		assertEquals(200, response.getStatusCode());
		assertEquals("2.4.0", response.getBody().asString());
		
	}
	
	/**
     * Tests the retrieving of the latest version number of a specific {@link PropertyGroup}
     */
    @Test
    public void nonExistsLatestVersionNumberTest() {
        
        final Response response = given().auth().basic("user", "user").when().get("/henge/v1/property-groups/PropertyGroup-Test5/versions/ceiling");
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-Test5].", response.getBody().asString());
        
    }
    

    /**
     * Tests the versions endpoint, to return a version Set of {@link String}
     */
    @Test
    public void versionsPropertyGroupTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/property-groups/PropertyGroup-Test1/versions");
        
        assertEquals(200, response.getStatusCode());
        assertEquals(3, response.getBody().as(Set.class).size());
    }
    
    /**
     * Tests the versions endpoint, to return a version Set of {@link String}
     */
    @Test
    public void nonFoundVersionsPropertyGroupTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/property-groups/PropertyGroup-Test2/versions");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No PropertyGroup was found by the given name [PropertyGroup-Test2].", response.getBody().asString());
    }
    
    @After
    public void tearDown() throws Exception {
    	cleanerUtils.execute();
    }
	
	
}
