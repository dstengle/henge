package com.kenzan.henge.resource.v1.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.http.ContentType;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.service.PropertyGroupBD;
import com.kenzan.henge.service.VersionSetBD;
import com.kenzan.henge.service.VersionSetMappingBD;
import com.kenzan.henge.util.AbstractIntegrationTest;
import com.kenzan.henge.util.CleanerUtils;


/**
 * @author Igor K. Shiohara
 */
public class ConvenienceRSIT extends AbstractIntegrationTest {

	@Autowired
	private PropertyGroupBD propertyGroupBD;
	
	@Autowired
	private VersionSetBD versionSetBD;
	
	@Autowired
	private VersionSetMappingBD mappingBD;
	
	@Autowired
	private CleanerUtils cleanerUtils;
	
	private static String successPayload = " { " + 
			"     \"propertyGroupList\" : [ " +
			"         {" +
			"           \"name\" : \"PropertyGroup-Test1\"," +
			"           \"version\" : \"1.0.1\"," +
			"           \"description\" : \"PropertyGroup-Test-description\"," +
			"           \"type\" : \"APP\"," +
			"           \"active\" : true," +
			"           \"createdDate\" : \"2015-11-23T09:44:51.580\"," +
			"           \"properties\" : [ {" +
			"             \"name\" : \"app-name\"," +
			"             \"description\" : \"Application Description\"," +
			"             \"defaultValue\" : null," +
			"             \"propertyScopedValues\" : [ {" +
			"               \"key\" : \"env=dev\"," +
			"               \"value\" : \"Pet Store App - Development\"" +
			"             }, {" +
			"               \"key\" : \"env=prod\"," +
			"               \"value\" : \"Pet Store App - Production\"" +
			"             } ] " +
			"           }, {" +
			"             \"name\" : \"property-foo\"," +
			"             \"description\" : \"Dummy Description\"," +
			"             \"defaultValue\" : null," +
			"             \"propertyScopedValues\" : [ {" +
			"               \"key\" : \"env=dev\"," +
			"               \"value\" : \"Bar Dev\"" +
			"             }, {" +
			"               \"key\" : \"env=prod\"," +
			"               \"value\" : \"Bar Prod\"" +
			"             } ]" +
			"           } ]" +
			"         }," +
			"         " +
			"         {" +
			"           \"name\" : \"PropertyGroup-Test2\"," +
			"           \"version\" : \"1.0.1\"," +
			"           \"description\" : \"PropertyGroup-Test2-description\"," +
			"           \"type\" : \"APP\"," +
			"           \"active\" : true," +
			"           \"createdDate\" : \"2015-11-23T09:44:51.580\"," +
			"           \"properties\" : [ {" +
			"             \"name\" : \"app-name\"," +
			"             \"description\" : \"Application Description\"," +
			"             \"defaultValue\" : null," +
			"             \"propertyScopedValues\" : [ {" +
			"               \"key\" : \"env=dev\"," +
			"               \"value\" : \"Pet Store App - Development\"" +
			"             }, {" +
			"               \"key\" : \"env=prod\"," +
			"               \"value\" : \"Pet Store App - Production\"" +
			"             } ] " +
			"           }, {" +
			"             \"name\" : \"property-foo\"," +
			"             \"description\" : \"Dummy Description\"," +
			"             \"defaultValue\" : null," +
			"             \"propertyScopedValues\" : [ {" +
			"               \"key\" : \"env=dev\"," +
			"               \"value\" : \"Bar Dev\"" +
			"             }, {" +
			"               \"key\" : \"env=prod\"," +
			"               \"value\" : \"Bar Prod\"" +
			"             } ]" +
			"           } ]" +
			"         }," +
			"         {" +
			"           \"name\" : \"PropertyGroup-Test3\"," +
			"           \"version\" : \"1.0.0\"," +
			"           \"description\" : \"PropertyGroup-Test3-description\"," +
			"           \"type\" : \"LIB\"," +
			"           \"active\" : true," +
			"           \"createdDate\" : null," +
			"           \"properties\" : [ {" +
			"             \"name\" : \"app-name\"," +
			"             \"description\" : \"Application Description\"," +
			"             \"defaultValue\" : null," +
			"             \"propertyScopedValues\" : [ {" +
			"               \"key\" : \"env=dev\"," +
			"               \"value\" : \"Pet Store App - Development\"" +
			"             }, {" +
			"               \"key\" : \"env=prod\"," +
			"               \"value\" : \"Pet Store App - Production\"" +
			"             } ] " +
			"           }, {" +
			"             \"name\" : \"property-foo\"," +
			"             \"description\" : \"Dummy Description\"," +
			"             \"defaultValue\" : null," +
			"             \"propertyScopedValues\" : [ {" +
			"               \"key\" : \"env=dev\"," +
			"               \"value\" : \"Bar Dev\"" +
			"             }, {" +
			"               \"key\" : \"env=prod\"," +
			"               \"value\" : \"Bar Prod\"" +
			"             } ]" +
			"           } ]" +
			"         }" +
			"     ]," +
			"     \"versionSetList\" : [" +
			"         {" +
			"           \"name\": \"VersionSet-Test\"," +
			"           \"version\": \"1.0.0\"," +
			"           \"description\": null," +
			"           \"propertyGroupReferences\": [" +
			"             {" +
			"               \"name\": \"PropertyGroup-Test3\"," +
			"               \"version\": \"1.0.0\"" +
			"             }" +
			"           ],   " +
			"           \"createdDate\": null," +
			"           \"scopedPropertyValueKeys\": null," +
			"           \"typeHierarchyEnabled\": true" +
			"         }    " +
			"     ]," +
			"     \"mappingList\" : [" +
			"         { " +
			"           \"application\" : \"PropertyGroup-Test1\"," +
			"           \"scopeString\" : \"env=env1,stack=stack1\"," +
			"           \"vsReference\" : {" +
			"               \"name\": \"VersionSet-Test\"," +
			"               \"version\": \"1.0.0\"" +
			"           }    " +
			"         }" +
			"     ]" +
			" }";
	
	/**
	 * Test: Insert PropertyGroup, VersionSet and Mapping in the same request
	 * Expected result: Status 200. All data created.
	 */
	@Test
	public void successTest() {
		given().auth().basic("user", "user")
		.body(successPayload)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
		.when().post("/henge/v1/convenience/batch");

		VersionSetReference versionSetReference = VersionSetReference.builder("VersionSet-Test", "1.0.0").build();
		
		final Map<MappingKey, VersionSetReference> expected = new HashMap<>();
		expected.put(new MappingKey(
				Sets.newHashSet(Scope.builder("env", "env1").build(),
								Scope.builder("stack", "stack1").build(),
								Scope.builder("application", "PropertyGroup-Test1").build())), versionSetReference
				);
		
		assertTrue(propertyGroupBD.read("PropertyGroup-Test1", "1.0.1").isPresent());
		assertTrue(propertyGroupBD.read("PropertyGroup-Test2", "1.0.1").isPresent());
		assertTrue(propertyGroupBD.read("PropertyGroup-Test3", "1.0.0").isPresent());
		assertTrue(versionSetBD.read("VersionSet-Test", "1.0.0").isPresent());
		assertTrue(mappingBD.getAllMappings().containsValue(versionSetReference));
	}
	
	/**
	 * Success test creating only a property group
	 */
	@Test
	public void propertyGroupTest() {
		final String json = "{\"propertyGroupList\" : [ "+
        "{ "+
        "  \"name\" : \"PropertyGroup-Test2\", "+
        "  \"version\" : \"1.0.1\", "+
        "  \"description\" : \"property-group-1-description\", "+
        "  \"type\" : \"APP\", "+
        "  \"active\" : true, "+
        "  \"createdDate\" : \"2015-11-23T09:44:51.580\", "+
        "  \"properties\" : [ { "+
        "    \"name\" : \"app-name\", "+
        "    \"description\" : \"Application Description\", "+
        "    \"defaultValue\" : null, "+
        "    \"propertyScopedValues\" : [ { "+
        "      \"key\" : \"env=dev\", "+
        "      \"value\" : \"Pet Store App - Development\" "+
        "    }, { "+
        "      \"key\" : \"env=prod\", "+
        "      \"value\" : \"Pet Store App - Production\" "+
        "    } ] "+ 
        "  } "+
        "]}"+
        "]}";
		
		given().auth().basic("user", "user")
		.body(json)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
		.when().post("/henge/v1/convenience/batch");
	}
	
	/**
	 * Tests the @Size annotation in the description field of {@link PropertyGroup}
	 */
	@Test
	public void invalidPropertyGroupTest() {
		final String smallDescription = "d";
		
		final String json = "{\"propertyGroupList\" : [ "+
        "{ "+
        "  \"name\" : \"property-group-1\", "+
        "  \"version\" : \"1.0.1\", "+
        "  \"description\" : \" "+ smallDescription + "\", "+
        "  \"type\" : \"APP\", "+
        "  \"active\" : true, "+
        "  \"createdDate\" : \"2015-11-23T09:44:51.580\", "+
        "  \"properties\" : [ { "+
        "    \"name\" : \"app-name\", "+
        "    \"description\" : \"Application Description\", "+
        "    \"defaultValue\" : null, "+
        "    \"propertyScopedValues\" : [ { "+
        "      \"key\" : \"env=dev\", "+
        "      \"value\" : \"Pet Store App - Development\" "+
        "    }, { "+
        "      \"key\" : \"env=prod\", "+
        "      \"value\" : \"Pet Store App - Production\" "+
        "    } ] "+ 
        "  } "+
        "]}";
		
		given().auth().basic("user", "user")
		.body(json)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(400)
		.when().post("/henge/v1/convenience/batch");
	}
	
	/**
	 * Tests the idempotency of the Convenience endpoint 
	 */
	@Test
	public void idempotencyTest() {
		given().auth().basic("user", "user")
		.body(successPayload)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
		.when().post("/henge/v1/convenience/batch");
		
		given().auth().basic("user", "user")
		.body(successPayload)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
		.when().post("/henge/v1/convenience/batch");
	}
	
	@Test
	public void propertyGroupAndVersionSetInRequestTest() {
		String payload = " { " + 
				"     \"propertyGroupList\" : [ " +
				"         {" +
				"           \"name\" : \"PropertyGroup-Test1\"," +
				"           \"version\" : \"1.0.0\"," +
				"           \"description\" : \"PropertyGroup-Test-description\"," +
				"           \"type\" : \"APP\"," +
				"           \"active\" : true," +
				"           \"createdDate\" : \"2015-11-23T09:44:51.580\"," +
				"           \"properties\" : [ {" +
				"             \"name\" : \"app-name\"," +
				"             \"description\" : \"Application Description\"," +
				"             \"defaultValue\" : null," +
				"             \"propertyScopedValues\" : [ {" +
				"               \"key\" : \"env=dev\"," +
				"               \"value\" : \"Pet Store App - Development\"" +
				"             }, {" +
				"               \"key\" : \"env=prod\"," +
				"               \"value\" : \"Pet Store App - Production\"" +
				"             } ] " +
				"           }, {" +
				"             \"name\" : \"property-foo\"," +
				"             \"description\" : \"Dummy Description\"," +
				"             \"defaultValue\" : null," +
				"             \"propertyScopedValues\" : [ {" +
				"               \"key\" : \"env=dev\"," +
				"               \"value\" : \"Bar Dev\"" +
				"             }, {" +
				"               \"key\" : \"env=prod\"," +
				"               \"value\" : \"Bar Prod\"" +
				"             } ]" +
				"           } ]" +
				"         }" +
				"     ]," +
				"     \"versionSetList\" : [" +
				"         {" +
				"           \"name\": \"VersionSet-Test1\"," +
				"           \"version\": \"1.0.0\"," +
				"           \"description\": null," +
				"           \"propertyGroupReferences\": [" +
				"             {" +
				"               \"name\": \"PropertyGroup-Test1\"," +
				"               \"version\": \"1.0.0\"" +
				"             }" +
				"           ],   " +
				"           \"createdDate\": null," +
				"           \"scopedPropertyValueKeys\": null," +
				"           \"typeHierarchyEnabled\": true" +
				"         }    " +
				"     ]}";
		
		given().auth().basic("user", "user")
		.body(payload)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
		.when().post("/henge/v1/convenience/batch");
	}
	
	/**
	 * Test: Only VersionSet in the request, referencing a non-existent PropertyGroupReference
	 * Expected result: Status 400. 
	 * Reason: PropertyGroup was not created in the request and doesn't exists in the repository
	 */
	@Test
	public void invalidPropertyGroupReferenceTest() throws JsonProcessingException {
		final String invalidPropertyGroup = "PropertyGroup-unexistent";
		
		final String versionSet = "{versionSetList\" : [" +
				"         {" +
				"           \"name\": \"VersionSet-Test\"," +
				"           \"version\": \"1.0.0\"," +
				"           \"description\": null," +
				"           \"propertyGroupReferences\": [" +
				"             {" +
				"               \"name\":" + invalidPropertyGroup +"," +
				"               \"version\": \"1.0.0\"" +
				"             }" +
				"           ],   " +
				"           \"createdDate\": null," +
				"           \"scopedPropertyValueKeys\": null," +
				"           \"typeHierarchyEnabled\": true" +
				"         }    " +
				"     ]}";
		
		given().auth().basic("user", "user")
		.body(versionSet)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(400)
		.when().post("/henge/v1/convenience/batch");
	}
	
	/**
	 * Test: Only VersionSet in the request, referencing a existent PropertyGroupReference in the repository
	 * Expected result: Status 200.
	 * Reason: PropertyGroup was not created in the request BUT exists in the repository
	 */
	@Test
	public void existentPropertyGroupReferenceTest() throws JsonProcessingException {
		final String propertyGroupName = "PropertyGroup-Test1";
		final PropertyGroup pg = PropertyGroup.builder(propertyGroupName, "1.0.0").build();
		propertyGroupBD.create(pg);
		
		final String versionSet = "{\"versionSetList\" : [" +
				"         {" +
				"           \"name\": \"VersionSet-Test1\"," +
				"           \"version\": \"1.0.0\"," +
				"           \"description\": null," +
				"           \"propertyGroupReferences\": [" +
				"             {" +
				"               \"name\":\"" + propertyGroupName +"\"," +
				"               \"version\": \"1.0.0\"" +
				"             }" +
				"           ],   " +
				"           \"createdDate\": null," +
				"           \"scopedPropertyValueKeys\": null," +
				"           \"typeHierarchyEnabled\": true" +
				"         }    " +
				"     ]}";
		
		given().auth().basic("user", "user")
		.body(versionSet)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
		.when().post("/henge/v1/convenience/batch");
	}
	
	/**
	 * Tests a version set with a small description to test the @Size validation
	 */
	@Test
	public void invalidVersionSetTest() {
		final String propertyGroupName = "PropertyGroup-Test1";
		final PropertyGroup pg = PropertyGroup.builder(propertyGroupName, "1.0.0").build();
		propertyGroupBD.create(pg);
		
		final String description = "d";
		
		final String json = "{\"versionSetList\" : [ " +
        " {" +
        "   \"name\": \"VersionSet0\"," +
        "   \"version\": \"1.0.0\"," +
        "   \"description\": \" " + description + "," +
        "   \"propertyGroupReferences\": [" +
        "     {" +
        "       \"name\": \"PropertyGroup-Test-1\"," +
        "       \"version\": \"1.0.0\"" +
        "     }" +
        "   ]," +   
        "   \"createdDate\": null," +
        "   \"scopedPropertyValueKeys\": null," +
        "   \"typeHierarchyEnabled\": true" +
        " }" +    
        " ]," +
        " \"mappingList\" : [" +
		" {" + 
		"   \"application\" : \"property-group-1\"," +
		"   \"scopeString\" : \"env=env1,stack=stack1\"," +
		"   \"vsReference\" : {" +
		"       \"name\": \"VersionSet0\"," +
		"       \"version\": \"1.0.0\"" +
		"   }" +    
		" }" +
		" ]}";
		
		given().auth().basic("user", "user")
		.body(json)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(400)
		.when().post("/henge/v1/convenience/batch");
	}
	
	/**
	 * Test: Only Mapping in the request, referencing a non-existent VersionSetReference
	 * Expected result: Status 400. 
	 * Reason: VersionSet was not created in the request and doesn't exists in the repository
	 */
	@Test
	public void invalidVersionSetReferenceTest() throws JsonProcessingException {
		final String invalidVersionSet = "VersionSet-Unexistent";
		
		final String map = "{mappingList\" : [" +
				"         { " +
				"           \"application\" : \"PropertyGroup-Test1\"," +
				"           \"scopeString\" : \"env=env1,stack=stack1\"," +
				"           \"vsReference\" : {" +
				"               \"name\":" + invalidVersionSet + "," +
				"               \"version\": \"1.0.0\"" +
				"           }    " +
				"         }" +
				"     ]}";
		
		given().auth().basic("user", "user")
		.body(map)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(400)
		.when().post("/henge/v1/convenience/batch");
	}
	
	/**
	 * Test: Only Mapping in the request, referencing a existent VersionSetReference in the repository
	 * Expected result: Status 200.
	 * Reason: VersionSet was not created in the request BUT exists in the repository
	 */
	@Test
	public void existentVersionSetReferenceTest() throws JsonProcessingException {
		final String vsName = "VersionSet-Test1";
		versionSetBD.create(VersionSet.builder(vsName, "1.0.0").build());
		
		final String map = "{\"mappingList\" : [" +
				"         { " +
				"           \"application\" : \"PropertyGroup-Test1\"," +
				"           \"scopeString\" : \"env=env1,stack=stack1\"," +
				"           \"vsReference\" : {" +
				"               \"name\":\"" + vsName + "\"," +
				"               \"version\": \"1.0.0\"" +
				"           }    " +
				"         }" +
				"     ]}";
		
		given().auth().basic("user", "user")
		.body(map)
		.with().contentType(ContentType.JSON)
		.then().expect()
			.statusCode(200)
		.when().post("/henge/v1/convenience/batch");
	}
	
	@After
    public void tearDown() throws Exception {
    	cleanerUtils.execute();
    }
	
}
