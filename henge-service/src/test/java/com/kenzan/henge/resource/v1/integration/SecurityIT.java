package com.kenzan.henge.resource.v1.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.kenzan.henge.Application;
import com.kenzan.henge.config.SecurityConfig;
import com.kenzan.henge.service.PropertyGroupBD;

/**
 * Integration test to check if the security configuration defined in {@link SecurityConfig} is working properly.
 * It should allow GET requests without authentication but require authentication for other HTTP methods. 
 * 
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@IntegrationTest("server.port = 0")
//@ActiveProfiles({"dev", "flatfile_local", "setmapping"})
@WebAppConfiguration
@TestPropertySource(properties={"security.user=testuser", "security.password=secret"})
public class SecurityIT {
    
    private static final String PROPERTY_GROUP_NAME = "PropertyGroup-Test";
	
    @Value("${local.server.port}")
    private int port;
    
    @Value("${security.user}")
    private String user;
    
    @Value("${security.password}")
    private String password;

    @Autowired
    private PropertyGroupBD propertyGroupBD;
    
    @Before
    public void setUp() {
        RestAssured.port = port;

        propertyGroupBD.delete(PROPERTY_GROUP_NAME);
    }   
	
	/**
	 * Tests calling an endpoint that requires authentication and provides the correct authentication 
	 */
	@Test
	public void createPropertyGroupTestSucessful() {
        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
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
              
            System.out.println("User: " + user);
            System.out.println("Pwd: " + password);
        
            given().auth().basic("testuser", "secret")
                  .body(jsonRequest)
                  .with().contentType(ContentType.JSON)
                  .then().expect()
                      .statusCode(200)
                      .body("name", equalTo(PROPERTY_GROUP_NAME),
                            "version", equalTo("1.0.1"),
                            "description", equalTo("property-group-1-description"),
                            "type", equalTo("APP"),
                            "active", is(true),
                            "createdDate", notNullValue()
                            )
                  .when().post("/henge/v1/property-groups");
	}
	
    /**
     * Tests calling an endpoint that requires authentication and provides the correct authentication 
     */
    @Test
    public void createPropertyGroupWithWrongCredentials() {
        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
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
              
            given().auth().basic("wronguser", "wrongpasswd")
                  .body(jsonRequest)
                  .with().contentType(ContentType.JSON)
                  .then().expect()
                      .statusCode(401)
                      .statusLine("HTTP/1.1 401 Unauthorized")
                  .when().post("/henge/v1/property-groups");
    }

    /**
     * Tests calling an endpoint that requires authentication and provides no authentication 
     */
    @Test
    public void createPropertyGroupWithNoCredentials() {
        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
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
              
            given()
                .body(jsonRequest)
                .with().contentType(ContentType.JSON)
                .then().expect()
                    .statusCode(401)
                    .statusLine("HTTP/1.1 401 Unauthorized")
                .when().post("/henge/v1/property-groups");
    }
    
    /**
     * Tests calling an endpoint that does not require authentication, no authentication and that it returns 200 
     */
    @Test
    public void getPropertyGroupWithNoCredentials() {

        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
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
              
            given().auth().basic("testuser", "secret")
                  .body(jsonRequest)
                  .with().contentType(ContentType.JSON)
                  .then().expect()
                      .statusCode(200)
                      .body("name", equalTo(PROPERTY_GROUP_NAME),
                            "version", equalTo("1.0.1"),
                            "description", equalTo("property-group-1-description"),
                            "type", equalTo("APP"),
                            "active", is(true),
                            "createdDate", notNullValue()
                            )
                  .when().post("/henge/v1/property-groups");

            given()
                .pathParam("property-group-name", PROPERTY_GROUP_NAME)
                .then().expect()
                    .statusCode(200)
                    .body("name", equalTo(PROPERTY_GROUP_NAME),
                          "version", equalTo("1.0.1"),
                          "description", equalTo("property-group-1-description"),
                          "type", equalTo("APP"),
                          "active", is(true),
                          "createdDate", notNullValue()
                          )
                .when().get("/henge/v1/property-groups/{property-group-name}/versions/latest");
        
    }
    
    /**
     * Tests calling an endpoint with PUT with no authentication and that it returns 401 
     */
    @Test
    public void updatePropertyGroupWithNoCredentials() {

        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
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
              
            given()
                .body(jsonRequest)
                .with().contentType(ContentType.JSON)
                .pathParam("property-group-name", PROPERTY_GROUP_NAME)
                .then().expect()
                    .statusCode(401)
                    .statusLine("HTTP/1.1 401 Unauthorized")
                .when().put("/henge/v1/property-groups/{property-group-name}");
        
    }    

    /**
     * Tests calling an endpoint with PUT supplying the correct credentials and checks that it returns 200 
     */
    @Test
    public void updatePropertyGroupSuccessful() {

        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
                        " \"version\" : \"1.0.0\", "+
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
              
        given().auth().basic("testuser", "secret")
        .body(jsonRequest)
        .with().contentType(ContentType.JSON)
        .then().expect()
            .statusCode(200)
            .body("name", equalTo(PROPERTY_GROUP_NAME),
                  "version", equalTo("1.0.0"),
                  "description", equalTo("property-group-1-description"),
                  "type", equalTo("APP"),
                  "active", is(true),
                  "createdDate", notNullValue()
                  )
        .when().post("/henge/v1/property-groups");
        
        jsonRequest = jsonRequest.replaceFirst("1\\.0\\.0", "1.0.1");

        given().auth().basic("testuser", "secret")
            .body(jsonRequest)
            .with().contentType(ContentType.JSON)
            .pathParam("property-group-name", PROPERTY_GROUP_NAME)
            .then().expect()
                .statusCode(200)
                .body("name", equalTo(PROPERTY_GROUP_NAME),
                      "version", equalTo("1.0.1"),
                      "description", equalTo("property-group-1-description"),
                      "type", equalTo("APP"),
                      "active", is(true),
                      "createdDate", notNullValue()
                      )
            .when().put("/henge/v1/property-groups/{property-group-name}");
        
    }    

    /**
     * Tests calling an endpoint with DELETE with no credentials and checks that it returns 401 
     */
    @Test
    public void deletePropertyGroupWithNoCredentials() {

        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
                        " \"version\" : \"1.0.0\", "+
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
              
        given().auth().basic("testuser", "secret")
        .body(jsonRequest)
        .with().contentType(ContentType.JSON)
        .then().expect()
            .statusCode(200)
            .body("name", equalTo(PROPERTY_GROUP_NAME),
                  "version", equalTo("1.0.0"),
                  "description", equalTo("property-group-1-description"),
                  "type", equalTo("APP"),
                  "active", is(true),
                  "createdDate", notNullValue()
                  )
        .when().post("/henge/v1/property-groups");
        
        given().auth().none()
            .pathParam("property-group-name", PROPERTY_GROUP_NAME)
            .then().expect()
                .statusCode(401)
                .statusLine("HTTP/1.1 401 Unauthorized")
            .when().delete("/henge/v1/property-groups/{property-group-name}");
        
    }    

    /**
     * Tests calling an endpoint with DELETE supplying the correct credentials and checks that it returns 200 
     */
    @Test
    public void deletePropertyGroupSuccessful() {

        String jsonRequest = "{ "+
                        " \"name\" : \"" + PROPERTY_GROUP_NAME + "\", "+ 
                        " \"version\" : \"1.0.0\", "+
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
              
        given().auth().basic("testuser", "secret")
        .body(jsonRequest)
        .with().contentType(ContentType.JSON)
        .then().expect()
            .statusCode(200)
            .body("name", equalTo(PROPERTY_GROUP_NAME),
                  "version", equalTo("1.0.0"),
                  "description", equalTo("property-group-1-description"),
                  "type", equalTo("APP"),
                  "active", is(true),
                  "createdDate", notNullValue()
                  )
        .when().post("/henge/v1/property-groups");
        
        given().auth().basic("testuser", "secret")
        .pathParam("property-group-name", PROPERTY_GROUP_NAME)
        .then().expect()
            .statusCode(200)
            .body("name", equalTo(PROPERTY_GROUP_NAME),
                  "version", equalTo("1.0.0"),
                  "description", equalTo("property-group-1-description"),
                  "type", equalTo("APP"),
                  "active", is(true),
                  "createdDate", notNullValue()
                  )
        .when().delete("/henge/v1/property-groups/{property-group-name}");
        
    }    

    @After
    public void tearDown() throws Exception {
        
        propertyGroupBD.delete(PROPERTY_GROUP_NAME);
        
    }
	
}
