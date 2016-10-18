package com.kenzan.henge.repository.mapping;

import com.google.common.collect.Sets;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.repository.MappingRepository;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.repository.VersionSetRepository;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test class tests if the mapping class implementation specified by the
 * ActiveProfiles annotation works as expected in regards to returning the best
 * generic mapping when an exact match is not found.
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({ "dev", "flatfile_local", "setmapping" })
public class FixturesGenerationTest {

    private static final String VERSION_100 = "1.0.0";
	private static final Logger LOGGER = LoggerFactory.getLogger(FixturesGenerationTest.class);
    private static final int NUMBER_OF_VERSION_SETS = 10;
    
    @Autowired
    private VersionSetRepository vsRepository;
    
    @Autowired
    private PropertyGroupRepository pgRepository;

    @Autowired
    private MappingRepository<VersionSetReference> mappingRepository;

    @Value("${user.home}${repository.location}")
    private String repositoryLocation;
    
    private static final String VERSION1 = VERSION_100;
    
    private PropertyGroup app;
    private PropertyGroup lib1;
    private PropertyGroup lib2;
    private PropertyGroup lib3;
    

    /**
     * This setup does the following things: - Creates 4 VersionSets with 10
     * PropertyGroupReferences each. - Creates the keys for each VersionSet and
     * inserts them into an array. - Inserts the VersionSetReferences in the
     * mapping. - Saves the mapping.
     * 
     * @throws java.lang.Exception if any exception occurs
     */
    @Before
    public void setUp() throws Exception {

        PropertyFixture fixture = new PropertyFixture();
        
        // Create and persists PropertyGroups
        for (int i = 0; i < NUMBER_OF_VERSION_SETS; i++) {
	        app = PropertyGroup.builder("PropertyGroup-App"+i, VERSION1)
	                .withDescription("App description")
	                .withType(PropertyGroupType.APP.toString())
	                .withIsActive(true)
	                .withProperties(fixture.getProperty1())
	                .build();  
	        pgRepository.create(app);
        }
        
        lib1 = PropertyGroup.builder("PropertyGroup-Lib-1", VERSION1)
                .withDescription("PropertyGroup-Lib-1 description")
                .withType(PropertyGroupType.LIB.toString())
                .withIsActive(true)
                .withProperties(fixture.getProperty2(), fixture.getProperty3())
            .build();   
        pgRepository.create(lib1);
        
        lib2 = PropertyGroup.builder("PropertyGroup-Lib-2", VERSION1)
                .withDescription("PropertyGroup-Lib-2 description")
                .withType(PropertyGroupType.LIB.toString())
                .withIsActive(true)
                .withProperties(fixture.getProperty4(), fixture.getProperty5())
            .build();   
        pgRepository.create(lib2);
        
        lib3 = PropertyGroup.builder("PropertyGroup-Lib-3", VERSION1)
                .withDescription("PropertyGroup-Lib-3 description")
                .withType(PropertyGroupType.LIB.toString())
                .withIsActive(true)
                .withProperties(fixture.getProperty6(), fixture.getProperty7(), fixture.getProperty8())
            .build();   
        pgRepository.create(lib3);
        
        // Create and persists VersionSets
        List<VersionSetReference> vsReferences = new LinkedList<>();
        for(int i=0; i < NUMBER_OF_VERSION_SETS; i++) {
            VersionSet versionSet = VersionSet.builder("VersionSet"+i, VERSION1)
                            .withDescription("VersionSet Description")
                            .withPropertyGroupReferences(PropertyGroupReference.builder("PropertyGroup-App"+i, VERSION1).build(),
                                                         PropertyGroupReference.builder("PropertyGroup-Lib-1", VERSION1).build(),
                                                         PropertyGroupReference.builder("PropertyGroup-Lib-2", VERSION1).build(),
                                                         PropertyGroupReference.builder("PropertyGroup-Lib-3", VERSION1).build())
                         .build();
            vsRepository.create(versionSet);
            vsReferences.add(VersionSetReference.builder(versionSet.getName(), versionSet.getVersion()).build());
        }
        
        /*
         * ScopePrecedenceConfiguration is as follows:
         * "env;env+region;env+region+stack;hostname;application"
         * 
         * Mappings are as follows: 
         * ("env=dev") => version-set-test-0
         * ("env=dev&region=region-1") => version-set-test-1
         * ("env=dev&region=region-2") => version-set-test-2
         * ("env=prod&region=region-3&stack=stack-1") => version-set-test-3
         * ("hostname=hostname-1") => version-set-test-4
         * ("env=dev&region=region-3") => version-set-test-5
         * ("env=dev&region=region-4") => version-set-test-6
         * ("env=prod&region=region-5&stack=stack-1") => version-set-test-7
         * ("env=prod&region=region-6&stack=stack-1") => version-set-test-8
         * ("env=prod&region=region-7&stack=stack-1") => version-set-test-9
         * 
         */
        List<MappingKey> keys = new LinkedList<>();
        // Instantiates the Keys
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=dev")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-1")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-2")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=prod,region=region-3,stack=stack-1")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("hostname=hostname-1")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-3")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-4")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=prod,region=region-5,stack=stack-1")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=prod,region=region-6,stack=stack-1")));
        keys.add(new MappingKey(ScopeUtils.parseScopeString("env=prod,region=region-7,stack=stack-1")));

        // Loads the mapping from the repository. 
        Mapping<VersionSetReference> mapping = mappingRepository.load();

        for (int i=0; i<keys.size(); i++) {
            mapping.put(keys.get(i), vsReferences.get(i));
        }

        // Persists the mapping. 
        mappingRepository.save(mapping);

    }
    
    @Test
    @Ignore
    public void testNothing() {}
    
    public static class PropertyFixture {
        
        private static Property property1;
        private static Property property2WithConflict;
        private static Property property2;
        private static Property property3;
        private static Property property4;
        private static Property property5;
        private static Property property6;
        private static Property property7;
        private static Property property8;

        public PropertyFixture() {
            Scope env_dev = Scope.builder("env", "dev").build();
            Scope env_prod = Scope.builder("env", "prod").build();
            Scope stack1 = Scope.builder("stack", "stack-1").build();
            Scope stack2 = Scope.builder("stack", "stack-2").build();
            Scope region1 = Scope.builder("region", "region-1").build();
            Scope region2 = Scope.builder("region", "region-2").build();
            
            PropertyScopedValue scopesCombination1 = PropertyScopedValue.builder(Sets.newHashSet(env_dev), "env-dev-value").build();
            PropertyScopedValue scopesCombination2 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, region1), "env-dev-region-1-value").build();
            PropertyScopedValue scopesCombination3 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, region2), "env-dev-region-2-value").build();
            PropertyScopedValue scopesCombination4 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, stack1, region1), "env-dev-stack-1-region-1").build();
            PropertyScopedValue scopesCombination5 = PropertyScopedValue.builder(Sets.newHashSet(env_prod, stack2, region1), "env-prod-stack-2-region-1").build();
            PropertyScopedValue scopesCombination6 = PropertyScopedValue.builder(Sets.newHashSet(env_prod, region1), "env-prod-region1-value").build();
            
            property1 = new Property.Builder("property-1").withDefaultValue("DefaultValue1").withDescription("Property-Description1").withScopedValues(scopesCombination1, scopesCombination2, scopesCombination3, scopesCombination4, scopesCombination5, scopesCombination6).build();
            property2WithConflict = new Property.Builder("property-2").withDefaultValue("ConflictedProperty").withDescription("Conflicted Property with Lib1").withScopedValues(scopesCombination2, scopesCombination4).build();
            property2 = new Property.Builder("property-2").withDefaultValue("DefaultValue2").withDescription("Property-Description2").withScopedValues(scopesCombination2, scopesCombination4).build();
            property3 = new Property.Builder("property-3").withDefaultValue("DefaultValue3").withDescription("Property-Description3").withScopedValues(scopesCombination3).build();
            property4 = new Property.Builder("property-4").withDefaultValue("DefaultValue4").withDescription("Property-Description4").withScopedValues(scopesCombination4).build();
            property5 = new Property.Builder("property-5").withDefaultValue("DefaultValue5").withDescription("Property-Description5").withScopedValues(scopesCombination5).build();
            property6 = new Property.Builder("property-6").withDefaultValue("DefaultValue6").withDescription("Property-Description6").withScopedValues(scopesCombination6).build();
            property7 = new Property.Builder("property-7").withDefaultValue("DefaultValue7").withDescription("Property-Description7").withScopedValues(scopesCombination6).build();
            property8 = new Property.Builder("property-8").withDefaultValue("DefaultValue8").withDescription("Property-Description8").withScopedValues(scopesCombination6).build();
        }

        public Property getProperty1() {
            return property1;
        }

        public Property getProperty2WithConflict() {
            return property2WithConflict;
        }

        public Property getProperty2() {
            return property2;
        }

        public Property getProperty3() {
            return property3;
        }

        public Property getProperty4() {
            return property4;
        }

        public Property getProperty5() {
            return property5;
        }

        public Property getProperty6() {
            return property6;
        }

        public Property getProperty7() {
            return property7;
        }

        public Property getProperty8() {
            return property8;
        }

    }    

}