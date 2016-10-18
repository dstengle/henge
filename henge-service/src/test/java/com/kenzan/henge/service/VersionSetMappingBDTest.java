package com.kenzan.henge.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.Sets;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.repository.impl.flatfile.FileNamingService;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Tests the VersionSetMappingBD implementation
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({ "dev", "flatfile_local", "setmapping"})
@TestPropertySource(properties={"versionset.mapping.file.name=test_version_set_mapping"})
public class VersionSetMappingBDTest {
    
    private static final int NUMBER_OF_PROPERTIES = 10;
    private static final int NUMBER_OF_APP_PROPERTY_GROUPS = 5;
    private static final int NUMBER_OF_LIB_PROPERTY_GROUPS = 10;
    private static final int NUMBER_OF_VERSION_SETS = 22;
    
    private static final String MAPPING_NAME = "version-set-mapping-test";
    
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
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private FileNamingService fileNamingService;
    
    private VersionSetReference[] vsReferences = new VersionSetReference[NUMBER_OF_VERSION_SETS];

    
    
    @Before
    public void setUp() {
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
            propertyGroups[i] = PropertyGroup.builder("test-property-group-" + i, "1.0.0")
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
            
            versionSets[i] = VersionSet.builder("test-version-set-" + i, "1.0.0")
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
     * ("env=env-0") => test-version-set-0
     * ("env=env-0,region=region-0") => test-version-set-1
     * ("env=env-0,region=region-0,stack=stack-0") => test-version-set-2
     * ("env=env-0,region=region-0,stack=stack-1") => test-version-set-3
     * ("env=env-0,region=region-0,stack=stack-2") => test-version-set-4
     * ("env=env-0,region=region-1") => test-version-set-5
     * ("env=env-0,region=region-1,stack=stack-0") => test-version-set-6
     * ("env=env-0,region=region-1,stack=stack-1") => test-version-set-7
     * ("env=env-0,region=region-1,stack=stack-2") => test-version-set-8
     * ("env=env-1") => test-version-set-9
     * ("env=env-1,region=region-0") => test-version-set-10
     * ("env=env-1,region=region-0,stack=stack-0") => test-version-set-11
     * ("env=env-1,region=region-0,stack=stack-1") => test-version-set-12
     * ("env=env-1,region=region-0,stack=stack-2") => test-version-set-13
     * ("env=env-1,region=region-1") => test-version-set-14
     * ("env=env-1,region=region-1,stack=stack-0") => test-version-set-15
     * ("env=env-1,region=region-1,stack=stack-1") => test-version-set-16
     * ("env=env-1,region=region-1,stack=stack-2") => test-version-set-17
     * ("hostname=hostname-0") => test-version-set-18
     * ("env=env-0,hostname=hostname-1") => test-version-set-19 
     * This mapping becomes unfetchable because it mixes sepparate ScopePrecedence configuration key groups. 
     * Adding the fact that we append the application name when doing the queries (application name is mandatory for queries), makes exact matching impossible.
     * TODO: Should we validate cases like the one above so it's not possible to add them? It seams bad to allow adding mappings that can never be fetched.
     * ("application=application-0") => test-version-set-20
     * ("application=application-1") => test-version-set-21
     * 
     * Query keys and expected version set are as follows: 
     * ("env=env-2") => absent (no match) 
     * ("env=env-0,region=region-1") => test-version-set-5 (exact match) 
     * ("env=env-0,region=region-2") => test-version-set-0 (fall back to ("env=env-0")) 
     * ("env=env-1,region=region-1,stack=stack-3") => test-version-set-14 (fall back to ("env=env-1,region=region-1"))
     * ("env=env-1,region=region-1,stack=stack-2") => test-version-set-17 (exact match) 
     * ("env=env-0,hostname=hostname-1") => test-version-set-0 (unfetchable entry case, so fall back to ("env=env-0"))
     * ("env=env-0,hostname=hostname-0") => test-version-set-18 (overwrite by most specific match)
     * ("hostname=hostname-1") => absent (no match, because there is no hostname=hostname-1 by itself)
     * ("application=application-0") => test-version-set-20 (exact match)
     * ("env=env-0,region=region-1,application=application-1") => test-version-set-21 (overwrite by most specificmatch)
     */
    @Test
    public void test() {
        
        Optional<VersionSet> vs;
        
        // ("env=env-2") => absent (no match)
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("env=env-2"));
        assertFalse(vs.isPresent());
        
        // ("env=env-0,region=region-1") => test-version-set-5 (exact match)
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,region=region-1"));
        assertEquals("test-version-set-5", vs.get().getName());
        
        // ("env=env-0,region=region-2") => test-version-set-0 (fall back to ("env=env-0")) 
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,region=region-2"));
        assertEquals("test-version-set-0", vs.get().getName());

        // ("env=env-1,region=region-1,stack=stack-3") => test-version-set-14 (fall back to ("env=env-1,region=region-1"))
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("env=env-1,region=region-1,stack=stack-3"));
        assertEquals("test-version-set-14", vs.get().getName());

        // ("env=env-1,region=region-1,stack=stack-2") => test-version-set-17 (exact match) 
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("env=env-1,region=region-1,stack=stack-2"));
        assertEquals("test-version-set-17", vs.get().getName());
        
        // ("env=env-0,hostname=hostname-1") => test-version-set-0 (unfetchable entry case, so fall back to ("env=env-0"))
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,hostname=hostname-1"));
        assertEquals("test-version-set-0", vs.get().getName());
        
        // ("env=env-0,hostname=hostname-0") => test-version-set-18 (overwrite by most specific match)
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,hostname=hostname-0"));
        assertEquals("test-version-set-18", vs.get().getName());
        
        // ("hostname=hostname-1") => absent (no match, because there is no hostname=hostname-1 by itself)
        vs = vsMappingBD.findMatch("unmatchableAppName", ScopeUtils.parseScopeString("hostname=hostname-1"));
        assertFalse(vs.isPresent());
        
        // ("application=application-0") => test-version-set-20 (exact match)
        vs = vsMappingBD.findMatch("application-0", null);
        assertEquals("test-version-set-20", vs.get().getName());
        
        // ("env=env-0,region=region-1,application=application-1") => test-version-set-21 (overwrite by most specificmatch)
        vs = vsMappingBD.findMatch("application-1", ScopeUtils.parseScopeString("env=env-0,region=region-1"));
        assertEquals("test-version-set-21", vs.get().getName());

    }
    
    /**
     * Deletes files created and removes all the mapping keys inserted during
     * the test.
     * 
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

        fileStorageService.deleteBeginningWith(fileNamingService.getPath(PropertyGroup.class), "test-property-group");
        fileStorageService.deleteBeginningWith(fileNamingService.getPath(VersionSet.class), "test-version-set");
        
        //Erases the mapping file
        fileStorageService.delete(StringUtils.EMPTY,MAPPING_NAME);

    }
    
}
