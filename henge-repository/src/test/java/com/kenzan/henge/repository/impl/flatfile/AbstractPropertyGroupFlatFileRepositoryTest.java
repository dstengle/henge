package com.kenzan.henge.repository.impl.flatfile;

import javax.validation.Validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

/**
 * Tests for the flat file implementation of the {@link PropertyGroupRepository} 
 *
 * @author wmatsushita
 * @author Igor K. Shiohara
 */
public abstract class AbstractPropertyGroupFlatFileRepositoryTest {
    
    @Autowired
    protected Validator validator;
	
	@Autowired
	protected PropertyGroupRepository propertyGroupRepository;
	
	@Autowired
	protected VersionSetRepository versionSetRepository;
	
	@Autowired
	protected JsonUtils jsonUtils;
	
	@Autowired
	protected FileStorageService fileStorageService;
	
	@Autowired
	protected FileNamingService fileNamingService;
	
	protected PropertyGroup expectedPropertyGroup;
	protected Property property1, property2;
	
	protected String randomToken;
	protected String defaultPropertyGroupName;
	protected String defaultVersionSet;
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	/**
	 * Setting up properties to fill the PropertyGroup object. It's the same ones for all of them.
	 */
	@Before
	public void setUp() {
	    
		property1 = new Property.Builder("app-name")
				.withDescription("Application Description")
				.withScopedValues(
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Pet Store App - Development").build(validator),
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Pet Store App - Production").build(validator)							
				)
				.withDefaultValue("Default value")
				.build();
		property2 = new Property.Builder("property-foo")
				.withDescription("Dummy Description")
				.withScopedValues(
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Bar Dev").build(validator), 
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Bar Prod").build(validator)
				)
				.withDefaultValue("Default value 2")
				.build();

	}
	
	/**
     * Erases the files created during the tests. For this to work, all the {@link PropertyGroup} instances created by this test file
     * must have the name starting with 'test-property-group'
     */
    @After
    public void tearDown() throws Exception {
        
        fileStorageService.deleteBeginningWith(fileNamingService.getPath(PropertyGroup.class), defaultPropertyGroupName);       
        fileStorageService.deleteBeginningWith(fileNamingService.getPath(VersionSet.class), defaultVersionSet);
        
    }
	
}
