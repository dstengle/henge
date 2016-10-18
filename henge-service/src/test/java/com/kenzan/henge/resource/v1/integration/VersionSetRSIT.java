package com.kenzan.henge.resource.v1.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
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
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.service.PropertyGroupBD;
import com.kenzan.henge.service.VersionSetBD;
import com.kenzan.henge.util.AbstractIntegrationTest;
import com.kenzan.henge.util.CleanerUtils;

/**
 * @author Igor K. Shiohara
 */
public class VersionSetRSIT extends AbstractIntegrationTest {
	
	@Autowired
	private VersionSetBD versionSetBD;
	
	@Autowired
	private PropertyGroupBD propertyGroupBD;
	
	@Autowired
	private CleanerUtils cleanerUtils;
	
	private static String VS_NAME = "VersionSet-Test1";
	private static String PG_NAME_APP = "PropertyGroup-Test-App";
	private static String PG_NAME_LIB_3 = "PropertyGroup-Test-Lib-3";
	private static String PG_NAME_LIB_2 = "PropertyGroup-Test-Lib-2";
	private static String PG_NAME_LIB_1 = "PropertyGroup-Test-Lib-1";
	private static String VERSION_1 = "1.0.0";
	private static String VERSION_2 = "2.0.1";
	private static String VERSION_3 = "2.4.0";
	
	/**
	 * Create some {@link VersionSet} to use in the Integration Tests
	 * @throws IOException
	 */
	@Before
	public void init() throws IOException {
		if (!versionSetBD.read(VS_NAME, VERSION_1).isPresent()) {
			versionSetBD.create(VersionSet.builder(VS_NAME, VERSION_1)
					.withDescription("VersionSet App description")
					.withCreatedBy("Igor K. Shiohara")
					.withPropertyGroupReferences(
							PropertyGroupReference.builder(PG_NAME_APP, "1.0.0").build(),
							PropertyGroupReference.builder(PG_NAME_LIB_1, "1.0.1").build())
				.build());
		}
		if (!versionSetBD.read(VS_NAME, VERSION_2).isPresent()) {
			versionSetBD.update(VS_NAME, VersionSet.builder(VS_NAME, VERSION_2)
					.withDescription("VersionSet App description")
					.withPropertyGroupReferences(
							PropertyGroupReference.builder(PG_NAME_APP, "1.0.0").build(),
							PropertyGroupReference.builder(PG_NAME_LIB_1, "1.0.1").build())
				.build());
		}
		if (!versionSetBD.read(VS_NAME, VERSION_3).isPresent()) {
			versionSetBD.update(VS_NAME, VersionSet.builder(VS_NAME, VERSION_3)
					.withDescription("VersionSet App description")
					.withPropertyGroupReferences(
							PropertyGroupReference.builder(PG_NAME_APP, "1.0.0").build(),
							PropertyGroupReference.builder(PG_NAME_LIB_1, "1.0.1").build())
				.build());
		}
		
		if (!propertyGroupBD.read(PG_NAME_APP, VERSION_1).isPresent()) {
			propertyGroupBD.create(PropertyGroup.builder(PG_NAME_APP, VERSION_1)
                .withDescription("PropertyGroup App description")
                .withCreatedBy("Igor K. Shiohara")
                .withIsActive(true)
                .withType(PropertyGroupType.APP.toString())
            .build());
		}
		
		if (!propertyGroupBD.read(PG_NAME_LIB_3, VERSION_1).isPresent()) {
			propertyGroupBD.create(PropertyGroup.builder(PG_NAME_LIB_3, VERSION_1)
                .withDescription("PropertyGroup App description")
                .withCreatedBy("Igor K. Shiohara")
                .withIsActive(true)
                .withType(PropertyGroupType.APP.toString())
            .build());
        }
		
		if (!propertyGroupBD.read(PG_NAME_LIB_2, VERSION_1).isPresent()) {
			propertyGroupBD.create(PropertyGroup.builder(PG_NAME_LIB_2, VERSION_1)
                .withDescription("PropertyGroup App description")
                .withCreatedBy("Igor K. Shiohara")
                .withIsActive(true)
                .withType(PropertyGroupType.APP.toString())
            .build());
        }
		
		if (!propertyGroupBD.read(PG_NAME_LIB_1, VERSION_1).isPresent()) {
			propertyGroupBD.create(PropertyGroup.builder(PG_NAME_LIB_1, VERSION_1)
                .withDescription("PropertyGroup App description")
                .withCreatedBy("Igor K. Shiohara")
                .withIsActive(true)
                .withType(PropertyGroupType.APP.toString())
            .build());
        }
	}
	
	/**
	 * Tests the creation of a new {@link VersionSet} endpoint 
	 */
	@Test
	public void createVersionSetTestSucessful() {
		String jsonRequest = "{ "+
				  "\"name\": \"VersionSet-TestCreate\", "+
				  "\"version\": \"1.0.0\", "+
				  "\"description\": \"VersionSet App description\", "+
				  "\"propertyGroupReferences\": ["+
				    "{ "+
				      "\"name\": \"PropertyGroup-Test-App\", "+
				      "\"version\": \"1.0.0\" "+
				    "}, "+
				    "{ "+
				      "\"name\": \"PropertyGroup-Test-Lib-3\", "+
				      "\"version\": \"1.0.0\""+ 
				    "},"+
				    "{"+
				      "\"name\": \"PropertyGroup-Test-Lib-2\","+
				      "\"version\": \"1.0.0\""+
				    "},"+
				    "{"+
				      "\"name\": \"PropertyGroup-Test-Lib-1\","+
				      "\"version\": \"1.0.0\""+
				    "}"+
				  "],"+
				  "\"createdDate\": null,"+
				  "\"scopedPropertyValueKeys\": null,"+
				"  \"typeHierarchyEnabled\": true"+
				"}";	
		
		given().auth().basic("user", "user")
			.body(jsonRequest)
			.with().contentType(ContentType.JSON)
			.then().expect()
				.statusCode(200)
				.body("name", equalTo("VersionSet-TestCreate"),
					  "version", equalTo("1.0.0"),
					  "description", equalTo("VersionSet App description")
					  )
			.when().post("/henge/v1/version-sets");
	}
	
	/**
	 * Tests the updating of an existent {@link VersionSet} endpoint
	 */
	@Test
	public void updateVersionSetTestSucessful() {
		versionSetBD.create(VersionSet.builder("VersionSet-TestUpdate", "1.0.0")
				.withDescription("VersionSet App description")
				.withCreatedBy("Igor K. Shiohara")
				.withPropertyGroupReferences(
						PropertyGroupReference.builder(PG_NAME_APP, "1.0.0").build(),
						PropertyGroupReference.builder(PG_NAME_LIB_1, "1.0.1").build())
			.build());
		
		String jsonRequest = "{ "+
				  "\"name\": \"VersionSet-TestUpdate\", "+
				  "\"version\": \"1.0.1\", "+
				  "\"description\": \"VersionSet App description\", "+
				  "\"propertyGroupReferences\": ["+
				    "{ "+
				      "\"name\": \"PropertyGroup-Test-App\", "+
				      "\"version\": \"1.0.0\" "+
				    "}, "+
				    "{ "+
				      "\"name\": \"PropertyGroup-Test-Lib-3\", "+
				      "\"version\": \"1.0.0\""+ 
				    "},"+
				    "{"+
				      "\"name\": \"PropertyGroup-Test-Lib-2\","+
				      "\"version\": \"1.0.0\""+
				    "},"+
				    "{"+
				      "\"name\": \"PropertyGroup-Test-Lib-1\","+
				      "\"version\": \"1.0.0\""+
				    "}"+
				  "],"+
				  "\"createdDate\": null,"+
				  "\"scopedPropertyValueKeys\": null,"+
				"  \"typeHierarchyEnabled\": true"+
				"}";	
		
		given().auth().basic("user", "user")
			.body(jsonRequest)
			.with().contentType(ContentType.JSON)
			.then().expect()
				.statusCode(200)
				.body("name", equalTo("VersionSet-TestUpdate"),
					  "version", equalTo("1.0.1"),
					  "description", equalTo("VersionSet App description")
					  )
			.when().put("/henge/v1/version-sets/VersionSet-TestUpdate");
	}
	
	/**
     * Tests the updating of an non existent {@link VersionSet}
     */
    @Test
    public void updateNonExistentVersionSetTest() {
        
        final String jsonRequest = "{ "+
                  "\"name\": \"VersionSet-TestUpdate\", "+
                  "\"version\": \"1.0.1\", "+
                  "\"description\": \"VersionSet App description\", "+
                  "\"propertyGroupReferences\": ["+
                    "{ "+
                      "\"name\": \"PropertyGroup-Test-App\", "+
                      "\"version\": \"1.0.0\" "+
                    "}, "+
                    "{ "+
                      "\"name\": \"PropertyGroup-Test-Lib-3\", "+
                      "\"version\": \"1.0.0\""+ 
                    "},"+
                    "{"+
                      "\"name\": \"PropertyGroup-Test-Lib-2\","+
                      "\"version\": \"1.0.0\""+
                    "},"+
                    "{"+
                      "\"name\": \"PropertyGroup-Test-Lib-1\","+
                      "\"version\": \"1.0.0\""+
                    "}"+
                  "],"+
                  "\"createdDate\": null,"+
                  "\"scopedPropertyValueKeys\": null,"+
                "  \"typeHierarchyEnabled\": true"+
                "}";    
        
        final Response response = given().auth().basic("user", "user")
            .body(jsonRequest)
            .with().contentType(ContentType.JSON)
            .when().put("/henge/v1/version-sets/VersionSet-TestUpdate");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No VersionSet was found by the given name [VersionSet-TestUpdate].", response.getBody().asString());
    }
	
	/**
	 * Tests the deletion of a new {@link VersionSet} endpoint 
	 * by {@link VersionSet} name
	 */
	@Test
	public void deleteVersionSetByNameTestSuccessful() {
		versionSetBD.create(VersionSet.builder("VersionSet-TestDelete", "1.0.1")
				.withDescription("VersionSet App description")
				.withCreatedBy("Igor K. Shiohara")
				.withPropertyGroupReferences(
						PropertyGroupReference.builder(PG_NAME_APP, "1.0.0").build(),
						PropertyGroupReference.builder(PG_NAME_LIB_1, "1.0.1").build())
			.build());
		
		given().auth().basic("user", "user")
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
			.body("name", equalTo("VersionSet-TestDelete"),
				  "version", equalTo("1.0.1"),
				  "description", equalTo("VersionSet App description")
				  )
		.when().delete("/henge/v1/version-sets/VersionSet-TestDelete");
	}
	
	/**
     * Tests the deletion of a non existent {@link VersionSet} 
     * by {@link VersionSet} name
     */
    @Test
    public void deleteNonExistentVersionSetByNameTest() {
        
        final Response response = given().auth().basic("user", "user")
        .with().contentType(ContentType.JSON)
        .when().delete("/henge/v1/version-sets/VersionSet-TestDelete");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No VersionSet was found by the given name [VersionSet-TestDelete].", response.getBody().asString());
    }
	
	/**
	 * Tests the deletion of a new {@link VersionSet} endpoint 
	 * by {@link VersionSet} name and version
	 */
	@Test
	public void deleteVersionSetByNameAndVersionTestSuccessful() {
		versionSetBD.create(VersionSet.builder("VersionSet-TestDelete2", "2.0.0")
				.withDescription("VersionSet App description")
				.withCreatedBy("Igor K. Shiohara")
				.withPropertyGroupReferences(
						PropertyGroupReference.builder(PG_NAME_APP, "1.0.0").build(),
						PropertyGroupReference.builder(PG_NAME_LIB_1, "1.0.1").build())
			.build());
		
		versionSetBD.update("VersionSet-TestDelete2", 
		    VersionSet.builder("VersionSet-TestDelete2", "2.0.1")
				.withDescription("VersionSet App description")
				.withCreatedBy("Igor K. Shiohara")
				.withPropertyGroupReferences(
						PropertyGroupReference.builder(PG_NAME_APP, "1.0.0").build(),
						PropertyGroupReference.builder(PG_NAME_LIB_1, "1.0.1").build())
			.build());
		
		given().auth().basic("user", "user")
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
			.body("name", equalTo("VersionSet-TestDelete2"),
				  "version", equalTo("2.0.1"),
				  "description", equalTo("VersionSet App description")
				  )
		.when().delete("/henge/v1/version-sets/VersionSet-TestDelete2/versions/2.0.1");
		
		Optional<VersionSet> v1 = versionSetBD.read("VersionSet-TestDelete2", "2.0.0");
		assertTrue(v1.isPresent());
		
		Optional<VersionSet> v2 = versionSetBD.read("VersionSet-TestDelete2", "2.0.1");
		assertFalse(v2.isPresent());
	}
	
	/**
     * Tests the deletion of a non existent {@link VersionSet} 
     * by {@link VersionSet} name and version
     */
    @Test
    public void deleteNonExistentVersionSetByNameAndVersionTest() {
        
        final Response response = given().auth().basic("user", "user")
        .with().contentType(ContentType.JSON)
        .when().delete("/henge/v1/version-sets/VersionSet-TestDelete2/versions/2.0.1");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No VersionSet was found by the given name [VersionSet-TestDelete2] and version [2.0.1] to be deleted.", response.getBody().asString());
    }
	
	/**
	 * Tests the read endpoint, to retrieve a {@link VersionSet}
	 */
	@Test
	public void readVersionSetByNameTestAndVersionSuccessful() {
		given().auth().basic("user", "user")
		.pathParam("VersionSetName", VS_NAME)
		.pathParam("VersionSetVersion", VERSION_2)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
			.body("name", equalTo(VS_NAME),
				  "version", equalTo(VERSION_2),
				  "description", equalTo("VersionSet App description")
				  )
		.when().get("/henge/v1/version-sets/{VersionSetName}/versions/{VersionSetVersion}");
	}
	
	/**
     * Tests the read endpoint, to a non existent {@link VersionSet}
     */
    @Test
    public void readNonExistentVersionSetByNameTestAndVersion() {
        
        final Response response = given().auth().basic("user", "user")
        .with().contentType(ContentType.JSON)
        .when().get("/henge/v1/version-sets/VersionSet-Test5/versions/2.0.1");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No VersionSet was found by the given name [VersionSet-Test5] and version [2.0.1].", response.getBody().asString());
    }
	
	/**
     * Tests the read endpoint, to retrieve a {@link VersionSet}
     */
    @Test
    public void readVersionSetByNameTestSuccessful() {
        
        given().auth().basic("user", "user")
        .with().contentType(ContentType.JSON)
        .then().expect()
            .statusCode(200)
            .body("name", equalTo("VersionSet-Test1"),
                  "version", equalTo("2.4.0"),
                  "description", equalTo("VersionSet App description")
                  )
        .when().get("/henge/v1/version-sets/VersionSet-Test1");
    }
    
    /**
     * Tests the read endpoint, to a non existent {@link VersionSet}
     */
    @Test
    public void readNonExistentVersionSetByNameTest() {
        
        final Response response = given().auth().basic("user", "user")
        .with().contentType(ContentType.JSON)
        .when().get("/henge/v1/version-sets/VersionSet-Test5");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No VersionSet was found by the given name [VersionSet-Test5].", response.getBody().asString());
    }
    
    /**
     * Tests the retrieving of the latest version number of a specific {@link VersionSet}
     */
    @Test
    public void latestVersionNumberTest() {
        
        final Response response = given().auth().basic("user", "user").when().get("/henge/v1/version-sets/VersionSet-Test1/versions/ceiling");
        assertEquals(200, response.getStatusCode());
        assertEquals("2.4.0", response.getBody().asString());
        
    }
    
    /**
     * Tests the retrieving of the latest version number of a specific {@link VersionSet}
     */
    @Test
    public void nonExistsLatestVersionNumberTest() {
        
        final Response response = given().auth().basic("user", "user").when().get("/henge/v1/version-sets/VersionSet-Test5/versions/ceiling");
        assertEquals(404, response.getStatusCode());
        assertEquals("No VersionSet was found by the given name [VersionSet-Test5].", response.getBody().asString());
        
    }
    
    /**
     * Tests the versions endpoint, to return a version Set of {@link String}
     */
    @Test
    public void versionsVersionSetTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/version-sets/VersionSet-Test1/versions");
        
        assertEquals(200, response.getStatusCode());
        assertEquals(3, response.getBody().as(Set.class).size());
    }
    
    /**
     * Tests the versions endpoint, to return a version Set of {@link String}
     */
    @Test
    public void nonFoundVersionsVersionSetTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/version-sets/VersionSet-Test2/versions");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No VersionSet was found by the given name [VersionSet-Test2].", response.getBody().asString());
    }
	
    @After
    public void tearDown() throws Exception {
    	cleanerUtils.execute();
    }
	
}
