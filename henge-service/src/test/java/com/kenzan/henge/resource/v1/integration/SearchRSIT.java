package com.kenzan.henge.resource.v1.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.jayway.restassured.RestAssured;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.service.VersionSetMappingBD;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Integration test for the SearchRS
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@WebAppConfiguration
@IntegrationTest("server.port = 0")
@ActiveProfiles({"dev", "flatfile_local", "setmapping"})
@TestPropertySource(properties={"versionset.mapping.file.name=test_version_set_mapping"})
public class SearchRSIT {

	@Value("${local.server.port}")
	private int port;
	
    private static final int NUMBER_OF_PROPERTIES = 10;
    private static final int NUMBER_OF_APP_PROPERTY_GROUPS = 5;
    private static final int NUMBER_OF_LIB_PROPERTY_GROUPS = 10;
    private static final int NUMBER_OF_VERSION_SETS = 22;
    
    private static final Scope[] ENVS = {Scope.builder("env", "env-0").build(), Scope.builder("env", "env-1").build(), Scope.builder("env", "env-2").build()};
    private static final Scope[] REGIONS = {Scope.builder("region", "region-0").build(), Scope.builder("region", "region-1").build(), Scope.builder("region", "region-2").build()};
    private static final Scope[] STACKS = {Scope.builder("stack", "stack-0").build(), Scope.builder("stack", "stack-1").build(), Scope.builder("stack", "stack-2").build()};
    private static final Scope[] HOSTNAMES = {Scope.builder("hostname", "hostname-0").build(), Scope.builder("hostname", "hostname-1").build()};

    @Autowired
    private PropertyGroupRepository pgRepo;
    
    @Autowired
    private VersionSetRepository vsRepo;
    
    @Autowired
    private VersionSetMappingBD vsMappingBD;
    
    @Value("${user.home}${repository.location}")
    private String repositoryLocation;

    @Value("${versionset.mapping.file.name}")
    private String mappingName;
    
    private VersionSetReference[] vsReferences = new VersionSetReference[NUMBER_OF_VERSION_SETS];
	
	@Before
	public void init() {
	    RestAssured.port = port;
	    
        Property[] properties = new Property[NUMBER_OF_PROPERTIES];
        
        //Build the Properties
        for(int i=0; i<NUMBER_OF_PROPERTIES; i++) {
            
            // Build all the combinations of PropertyScopedValues
            List<PropertyScopedValue> scopedValues = new ArrayList<PropertyScopedValue>(ENVS.length * REGIONS.length * STACKS.length);
            for(int e=0; e<ENVS.length; e++) {
                
                scopedValues.add(PropertyScopedValue.builder(
                    Sets.newHashSet(ENVS[e])
                    , new StringBuilder("env=env-").append(e).append("_value").toString()
                ).build());

                for(int r=0; r<REGIONS.length; r++) {
                    
                    scopedValues.add(PropertyScopedValue.builder(
                        Sets.newHashSet(ENVS[e], REGIONS[r])
                        , new StringBuilder("env=env-").append(e).append(",region=region-").append(r).append("_value").toString()
                    ).build());

                    for(int s=0; s<STACKS.length; s++) {
                        
                        scopedValues.add(PropertyScopedValue.builder(
                            Sets.newHashSet(ENVS[e], REGIONS[r], STACKS[s])
                            , new StringBuilder("env=env-").append(e).append(",region=region-").append(r).append(",stack=stack-").append(s).append("_value").toString()
                        ).build());
                        
                    }
                    
                }
                
            }
            
            properties[i] = new Property.Builder("property-" + i)
                .withDefaultValue("default-value")
                .withScopedValues(scopedValues.toArray(new PropertyScopedValue[0]))
                .build();
            
        }
        
        
        // Create all the PropertyGroups and PropertyGroupReferences
        PropertyGroup[] propertyGroups = new PropertyGroup[NUMBER_OF_APP_PROPERTY_GROUPS + NUMBER_OF_LIB_PROPERTY_GROUPS];
        PropertyGroupReference[] pgReferences = new PropertyGroupReference[NUMBER_OF_APP_PROPERTY_GROUPS + NUMBER_OF_LIB_PROPERTY_GROUPS];
        
        for(int i=0; i<(NUMBER_OF_APP_PROPERTY_GROUPS + NUMBER_OF_LIB_PROPERTY_GROUPS); i++) {
            
            // First 10 PropertyGroups are APP the rest are LIB
            String type = (i < NUMBER_OF_APP_PROPERTY_GROUPS)? PropertyGroupType.APP.toString() : PropertyGroupType.LIB.toString();
            propertyGroups[i] = PropertyGroup.builder("property-group-test-" + i, "1.0.0")
                .withIsActive(true)
                .withType(type)
                .withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.now())
                .withProperties(properties)
                .build();
         
            pgRepo.create(propertyGroups[i]);
            
            pgReferences[i] = PropertyGroupReference.builder(propertyGroups[i]).build();
            
        }
        
        VersionSet[] versionSets = new VersionSet[NUMBER_OF_VERSION_SETS];
        for(int i=0; i<NUMBER_OF_VERSION_SETS; i++) {
            
            int appIndex = i % NUMBER_OF_APP_PROPERTY_GROUPS;
            int libIndexes[] = {
                NUMBER_OF_APP_PROPERTY_GROUPS + (i % NUMBER_OF_LIB_PROPERTY_GROUPS), 
                NUMBER_OF_APP_PROPERTY_GROUPS + ((i+1) % NUMBER_OF_LIB_PROPERTY_GROUPS), 
                NUMBER_OF_APP_PROPERTY_GROUPS + ((i+2) % NUMBER_OF_LIB_PROPERTY_GROUPS)
            }; 
            
            versionSets[i] = VersionSet.builder("version-set-test-" + i, "1.0.0")
            .withCreatedBy("Wagner Y. Matsushita")
            .withCreatedDate(LocalDateTime.now())
            .withPropertyGroupReferences(pgReferences[appIndex], pgReferences[libIndexes[0]], pgReferences[libIndexes[1]], pgReferences[libIndexes[2]])
            .build();
            
            vsRepo.create(versionSets[i]);
            
            vsReferences[i] = VersionSetReference.builder(versionSets[i]).build();
            
        }
        
        
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0]),                        vsReferences[0]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[0]),            vsReferences[1]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[0], STACKS[0]), vsReferences[2]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[0], STACKS[1]), vsReferences[3]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[0], STACKS[2]), vsReferences[4]);
        
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[1]),            vsReferences[5]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[1], STACKS[0]), vsReferences[6]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[1], STACKS[1]), vsReferences[7]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], REGIONS[1], STACKS[2]), vsReferences[8]);
        
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1]),                        vsReferences[9]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[0]),            vsReferences[10]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[0], STACKS[0]), vsReferences[11]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[0], STACKS[1]), vsReferences[12]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[0], STACKS[2]), vsReferences[13]);

        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[1]),            vsReferences[14]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[1], STACKS[0]), vsReferences[15]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[1], STACKS[1]), vsReferences[16]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[1], REGIONS[1], STACKS[2]), vsReferences[17]);
        
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(HOSTNAMES[0]),                   vsReferences[18]);
        vsMappingBD.setMapping(Optional.empty(), Sets.newHashSet(ENVS[0], HOSTNAMES[1]),          vsReferences[19]);

        // Two direct mapping to specific applications
        vsMappingBD.setMapping(Optional.of("application-0"), null,                                 vsReferences[20]);
        vsMappingBD.setMapping(Optional.of("application-1"), null,                                 vsReferences[21]);

	}
	
    /**
     * ScopePrecedenceConfiguration is as follows:
     * "env;env+region;env+region+stack;hostname;application"
     * 
     * Mappings are as follows: 
     * ("env=env-0") => version-set-test-0
     * ("env=env-0,region=region-0") => version-set-test-1
     * ("env=env-0,region=region-0,stack=stack-0") => version-set-test-2
     * ("env=env-0,region=region-0,stack=stack-1") => version-set-test-3
     * ("env=env-0,region=region-0,stack=stack-2") => version-set-test-4
     * ("env=env-0,region=region-1") => version-set-test-5
     * ("env=env-0,region=region-1,stack=stack-0") => version-set-test-6
     * ("env=env-0,region=region-1,stack=stack-1") => version-set-test-7
     * ("env=env-0,region=region-1,stack=stack-2") => version-set-test-8
     * ("env=env-1") => version-set-test-9
     * ("env=env-1,region=region-0") => version-set-test-10
     * ("env=env-1,region=region-0,stack=stack-0") => version-set-test-11
     * ("env=env-1,region=region-0,stack=stack-1") => version-set-test-12
     * ("env=env-1,region=region-0,stack=stack-2") => version-set-test-13
     * ("env=env-1,region=region-1") => version-set-test-14
     * ("env=env-1,region=region-1,stack=stack-0") => version-set-test-15
     * ("env=env-1,region=region-1,stack=stack-1") => version-set-test-16
     * ("env=env-1,region=region-1,stack=stack-2") => version-set-test-17
     * ("hostname=hostname-0") => version-set-test-18
     * ("env=env-0,hostname=hostname-1") => version-set-test-19 
     * This mapping becomes unfetchable because it mixes sepparate ScopePrecedence configuration key groups. 
     * Adding the fact that we append the application name when doing the queries (application name is mandatory for queries), makes exact matching impossible.
     * TODO: Should we validate cases like the one above so it's not possible to add them? It seams bad to allow adding mappings that can never be fetched.
     * ("application=application-0") => version-set-test-20
     * ("application=application-1") => version-set-test-21
     * 
     * Query keys and expected version set are as follows: 
     * ("env=env-2") => absent (no match) 
     * ("env=env-0,region=region-1") => version-set-test-5 (exact match) 
     * ("env=env-0,region=region-2") => version-set-test-0 (fall back to ("env=env-0")) 
     * ("env=env-1,region=region-1,stack=stack-3") => version-set-test-14 (fall back to ("env=env-1,region=region-1"))
     * ("env=env-1,region=region-1,stack=stack-2") => version-set-test-17 (exact match) 
     * ("env=env-0,hostname=hostname-1") => version-set-test-0 (unfetchable entry case, so fall back to ("env=env-0"))
     * ("env=env-0,hostname=hostname-0") => version-set-test-18 (overwrite by most specific match)
     * ("hostname=hostname-1") => absent (no match, because there is no hostname=hostname-1 by itself)
     * ("application=application-0") => version-set-test-20 (exact match)
     * ("env=env-0,region=region-1,application=application-1") => version-set-test-21 (overwrite by most specificmatch)
     */
	@Test
    public void testCorrectVersionSet() {
        String response;
	    
        // ("env=env-2") => absent (no match)
        given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-2").then().statusCode(404);
    
        // ("env=env-0,region=region-1") => version-set-test-5 (exact match)
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-0,region=region-1").asString();
        assertTrue(response.contains("# version-set name: version-set-test-5"));
        
        // ("env=env-0,region=region-2") => version-set-test-0 (fall back to ("env=env-0")) 
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-0,region=region-2").asString();
        assertTrue(response.contains("# version-set name: version-set-test-0"));
        
        // ("env=env-1,region=region-1,stack=stack-3") => version-set-test-14 (fall back to ("env=env-1,region=region-1"))
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-1,region=region-1,stack=stack-3").asString();
        assertTrue(response.contains("# version-set name: version-set-test-14"));
        
        // ("env=env-1,region=region-1,stack=stack-2") => version-set-test-17 (exact match) 
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-1,region=region-1,stack=stack-2").asString();
        assertTrue(response.contains("# version-set name: version-set-test-17"));

        // ("env=env-0,hostname=hostname-1") => version-set-test-0 (unfetchable entry case, so fall back to ("env=env-0"))
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-0,hostname=hostname-1").asString();
        assertTrue(response.contains("# version-set name: version-set-test-0"));

        // ("env=env-0,hostname=hostname-0") => version-set-test-18 (overwrite by most specific match)
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-0,hostname=hostname-0").asString();
        assertTrue(response.contains("# version-set name: version-set-test-18"));

        // ("hostname=hostname-1") => absent (no match, because there is no hostname=hostname-1 by itself)
        given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=hostname=hostname-1").then().statusCode(404);

        // ("application=application-0") => version-set-test-20 (exact match)
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/application-0").asString();
        assertTrue(response.contains("# version-set name: version-set-test-20"));

        // ("env=env-0,region=region-1,application=application-1") => version-set-test-21 (overwrite by most specificmatch)
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/application-1?scopes=env=env-0,region=region-1").asString();
        assertTrue(response.contains("# version-set name: version-set-test-21"));

    }
	
	@Test
	public void testCorrectPropertyEvaluation() {
	    String response;
	    
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-0,region=region-2").asString();
        assertTrue(response.contains("property-0=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-1=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-2=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-3=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-4=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-5=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-6=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-7=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-8=env=env-0,region=region-2_value"));
        assertTrue(response.contains("property-9=env=env-0,region=region-2_value"));
        
        response = given().auth().basic("user", "user").when().get("/henge/v1/search/unmatchableAppName?scopes=env=env-1,region=region-1,stack=stack-3").asString();
        assertTrue(response.contains("property-0=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-1=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-2=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-3=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-4=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-5=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-6=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-7=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-8=env=env-1,region=region-1_value"));
        assertTrue(response.contains("property-9=env=env-1,region=region-1_value"));
        
	}
	
    /**
     * Deletes files created and removes all the mapping keys inserted during
     * the test.
     * 
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

        Path dir = FileSystems.getDefault().getPath(repositoryLocation, "PropertyGroup");
        eraseFilesNamed(dir, "*property-group-test*");
        
        dir = FileSystems.getDefault().getPath(repositoryLocation, "VersionSet");
        eraseFilesNamed(dir, "*version-set-test*");

        //Erases the mapping file
        Path mappingFile = FileSystems.getDefault().getPath(repositoryLocation, mappingName);
        Files.deleteIfExists(mappingFile);

    }
    
    private void eraseFilesNamed(Path dir, String... names) {
        
        for(String name : names) {
            try (DirectoryStream<Path> pathList = Files.newDirectoryStream(dir, name)) {
                for (Path path : pathList) {
                    Files.deleteIfExists(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
	
}
