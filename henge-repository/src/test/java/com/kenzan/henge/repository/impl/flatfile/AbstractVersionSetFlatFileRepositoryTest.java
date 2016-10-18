package com.kenzan.henge.repository.impl.flatfile;

import java.time.LocalDateTime;

import javax.validation.Validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;


/**
 * This class provides tests for the flat file implementation of the CRUD of VersionSets.
 *
 * @author wmatsushita
 */
public abstract class AbstractVersionSetFlatFileRepositoryTest {
    
    @Autowired
    protected Validator validator;
    
    @Autowired
    protected VersionSetRepository versionSetRepository;
    
    @Autowired
    protected PropertyGroupRepository propertyGroupRepository;
    
    @Autowired
    protected JsonUtils jsonUtils;
    
    @Autowired
    protected FileStorageService fileStorageService;
    
    @Autowired
    protected FileNamingService fileNamingService;
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    protected VersionSet expectedVersionSet;
    protected PropertyGroup propertyGroup1, propertyGroup2;
    protected Property property1, property2;
    protected PropertyGroupReference pgReference1, pgReference2;
    
    protected String randomToken; 
    protected String defaultPropertyGroupName; 
    protected String defaultVersionSet; 
    
    
    /**
     * Setting up properties to fill the VersionSet object. It's the same ones for all of them.
     */
    @Before
    public void setUp() {

        property1 = new Property.Builder("app-name")
                .withDescription("Application Description")
                .withScopedValues(
                        PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Pet Store App - Development").build(),
                        PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Pet Store App - Production").build()                           
                )
                .withDefaultValue("Default value")
                .build();
        property2 = new Property.Builder("property-foo")
                .withDescription("Dummy Description")
                .withScopedValues(
                        PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Bar Dev").build(), 
                        PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Bar Prod").build()
                )
                .withDefaultValue("Default value 2")
                .build();
        
        propertyGroup1 = PropertyGroup.builder(defaultPropertyGroupName + "1", "1.0.0")
                .withDescription("property-group-1-description")
                .withType(PropertyGroupType.APP.name())
                .withIsActive(true)
                .withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.now())
                .withProperties(property1, property2)
                .build(validator);       

        propertyGroup2 = PropertyGroup.builder(defaultPropertyGroupName + "2", "1.0.0")
                .withDescription("property-group-1-description")
                .withType(PropertyGroupType.APP.name())
                .withIsActive(true)
                .withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.now())
                .withProperties(property1, property2)
                .build(validator);
        
        propertyGroupRepository.create(propertyGroup1);
        
        propertyGroupRepository.create(propertyGroup2);
        
        pgReference1 = PropertyGroupReference.builder(defaultPropertyGroupName + "1", "1.0.0").build(validator);

        pgReference2 = PropertyGroupReference.builder(defaultPropertyGroupName + "2", "1.0.0").build(validator);

    }

    /**
     * Erases the files created during the tests. For this to work, all the {@link VersionSet} instances created by this test file
     * must have the name starting with 'test-version-set'
     */
    @After
    public void tearDown() throws Exception {
        
        fileStorageService.deleteBeginningWith(fileNamingService.getPath(PropertyGroup.class), defaultPropertyGroupName);
        fileStorageService.deleteBeginningWith(fileNamingService.getPath(VersionSet.class), defaultVersionSet);
        
    }

}
