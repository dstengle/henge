package com.kenzan.henge.repository.impl.flatfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Sets;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.PropertyGroupRepository;

/**
 * Tests for the flat file implementation of the {@link PropertyGroupRepository} 
 *
 * @author wmatsushita
 * @author Igor K. Shiohara
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","flatfile_local","setmapping"})
public class PropertyGroupLocalFlatFileRepositoryTest extends AbstractPropertyGroupFlatFileRepositoryTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyGroupLocalFlatFileRepositoryTest.class);
    
    @Value("${user.home}${repository.location}")
    private String repositoryLocation;
    
    @Value("${text.encoding}")
    private String textEncoding;
    
    public PropertyGroupLocalFlatFileRepositoryTest() {
        randomToken = "-"+ new Random().nextInt(1000000-1)+1;
        defaultPropertyGroupName = "test-property-group" + randomToken;
        defaultVersionSet = "test-version-set" + randomToken;
    }
	
	/**
	 * Tests creation of a new {@link PropertyGroup} by attempting to create it and then read it's contents and 
	 * comparing for equality to the json serialized from the same PropertyGroup instance used to generate the file
	 */
	@Test
	public void testCreateNewPropertyGroup() throws Exception {
		
		expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
				.withDescription("property-group-1-description")
				.withType(PropertyGroupType.APP.name())
				.withIsActive(true)
				.withCreatedBy("Wagner Y. Matsushita")
				.withCreatedDate(LocalDateTime.now())
				.withProperties(property1, property2)
				.build(validator);		

		propertyGroupRepository.create(expectedPropertyGroup);
		
		final String content = new String(
				Files.readAllBytes(
						FileSystems.getDefault().getPath(repositoryLocation, 
						        fileNamingService.getPath(PropertyGroup.class),
								fileNamingService.getCompleteFileName( 
								    expectedPropertyGroup.getName(), "1.0.0"))
				), Charset.forName(textEncoding)
		).trim();
		
		LOGGER.debug("Content: " + content);
		assertThat(content).isEqualTo(jsonUtils.toJson(PropertyGroup.builder(expectedPropertyGroup).build(validator)));
			
	}
	
	/**
	 * Test that when attempting to create a {@link PropertyGroup} that already has an existing version, 
	 * the proper exception is thrown
	 */
	@Test
	public void testCreateAlreadyExistingProperty() {
		
	    exception.expect(HengeValidationException.class);
		exception.expectMessage("The PropertyGroup being created already exists. Consider using the update method.");
	    
        expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
        .withDescription("property-group-1-description")
        .withType(PropertyGroupType.APP.name())
        .withIsActive(true)
        .withCreatedBy("Wagner Y. Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withProperties(property1, property2)
        .build(validator);       

        propertyGroupRepository.create(expectedPropertyGroup);
        
        expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.1")
        .withDescription("property-group-1-description")
        .withType(PropertyGroupType.APP.name())
        .withIsActive(true)
        .withCreatedBy("Wagner Y. Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withProperties(property1, property2)
        .build(validator);       

        propertyGroupRepository.create(expectedPropertyGroup);
        
	}
		
	/**
	 * Saves an expected PropertyGroup object, reads it back and checks for equality
	 */
	@Test
	public void testReadFile() {
		
		expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
				.withDescription("property-group-1-description")
				.withType(PropertyGroupType.APP.name())
				.withIsActive(true)
				.withCreatedBy("Wagner Y. Matsushita")
				.withCreatedDate(LocalDateTime.now())
				.withProperties(property1, property2)
				.build(validator);		

		propertyGroupRepository.create(expectedPropertyGroup);
		
		final PropertyGroup propertyGroup = propertyGroupRepository.read(expectedPropertyGroup.getName(), expectedPropertyGroup.getVersion()).get();
		assertThat(propertyGroup).isEqualTo(expectedPropertyGroup);
		
	}
	
	/**
	 * Saves an expected PropertyGroup object with a specific version, reads it back and checks for equality
	 */
	@Test
	public void testReadFileWithSpecificVersion() {
		
	    final PropertyGroup newPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
				.withDescription("property-group-1-description")
				.withType(PropertyGroupType.APP.name())
				.withIsActive(true)
				.withCreatedBy("Wagner Y. Matsushita")
				.withCreatedDate(LocalDateTime.now())
				.withProperties(property1, property2)
				.build(validator);	
		
		expectedPropertyGroup = PropertyGroup.builder(newPropertyGroup).withVersion("1.0.1").build(validator);	

		propertyGroupRepository.create(newPropertyGroup);
		propertyGroupRepository.update(defaultPropertyGroupName, expectedPropertyGroup);
		
		PropertyGroup propertyGroup;
		
		propertyGroup = propertyGroupRepository.read(expectedPropertyGroup.getName()).get();
		assertThat(propertyGroup).isEqualTo(expectedPropertyGroup);
		
		propertyGroup = propertyGroupRepository.read(newPropertyGroup.getName(), "1.0.0").get();
		assertThat(propertyGroup).isEqualTo(newPropertyGroup);
	}
	
	/**
	 * Tests that an attempt to read a non existing {@link PropertyGroup} fails in the right way, 
	 * by throwing {@link HengeResourceNotFoundException}.
	 */
	@Test
	public void testReadNonExistingFile() {

		Optional<PropertyGroup> entity = propertyGroupRepository.read("thisPropertyDoesNotExist", "thisVersionDoesNotExist");
		assertThat(entity.isPresent()).isFalse();
		
	}
	
	/**
	 * Creates the PropertyGroup and tries to update it then checks that it was updated. 
	 */
	@Test
	public void testCorrectUpdate() throws Exception {
		
		expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
			.withDescription("property-group-1-description")
			.withType(PropertyGroupType.APP.name())
			.withIsActive(true)
			.withCreatedBy("Wagner Y. Matsushita")
			.withCreatedDate(LocalDateTime.now())
			.withProperties(property1, property2)
			.build(validator);
	
		propertyGroupRepository.create(expectedPropertyGroup);
		
		expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.1")
			.withDescription("property-group-1-description")
			.withType(PropertyGroupType.APP.name())
			.withIsActive(true)
			.withCreatedBy("Wagner Y. Matsushita")
			.withCreatedDate(LocalDateTime.now())
			.withProperties(property1, property2)
			.build(validator);
	
		propertyGroupRepository.update(defaultPropertyGroupName, expectedPropertyGroup);
		
		final String content = new String(
				Files.readAllBytes(
						FileSystems.getDefault().getPath(
						    repositoryLocation, 
                            fileNamingService.getPath(PropertyGroup.class),
							fileNamingService.getCompleteFileName(
							    expectedPropertyGroup.getName(), expectedPropertyGroup.getVersion()))
				), Charset.forName(textEncoding)
		).trim();
		
		LOGGER.debug("Content: " + content);
        assertThat(content).isEqualTo(jsonUtils.toJson(
            PropertyGroup.builder(expectedPropertyGroup).build(validator)));
			
	}
	
	/**
	 * Tests deletion of a {@link PropertyGroup} by passing a non existing name and version. 
	 * The expected behavior is to throw a {@link HengeResourceNotFoundException}.
	 */
	@Test
	public void testDeleteNonExistentFile() {
		
	    final Optional<PropertyGroup> entity = propertyGroupRepository.delete("This name does not exist", "This version does not exist");
		assertThat(entity.isPresent()).isFalse();
		
	}
	
	/**
	 * Tests deletion of a {@link PropertyGroup} by creating a new one, checking that the file exists 
	 * and then deleting it and checking that the file no longer exists.
	 */
	@Test
	public void testDelete() {
		
	    expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
		.withDescription("property-group-1-description")
		.withType(PropertyGroupType.APP.name())
		.withIsActive(true)
		.withCreatedBy("Wagner Y. Matsushita")
		.withCreatedDate(LocalDateTime.now())
		.withProperties(property1, property2)
		.build(validator);
		
		propertyGroupRepository.create(expectedPropertyGroup);
		
		final Path path = FileSystems.getDefault().getPath(repositoryLocation,
            fileNamingService.getPath(PropertyGroup.class),
			fileNamingService.getCompleteFileName( 
						expectedPropertyGroup.getName(), 
						expectedPropertyGroup.getVersion()));
		assertThat(Files.exists(path)).isTrue();
		
		propertyGroupRepository.delete(expectedPropertyGroup.getName(), expectedPropertyGroup.getVersion());
		
		assertThat(Files.exists(path)).isFalse();
	}
	
	/**
	 * Test the PropertyGroup verification that is not referenced
	 * in a existing VersionSet 
	 */
	@Test
	public void testDeletePropertyGroupReferencedInAVersionSet() {
		
	    exception.expect(HengeValidationException.class);
		exception.expectMessage("You can not delete this property group because there are referenced VersionSets");

		createPropertyGroup(defaultPropertyGroupName, "1.0.0");
		createVersionSet(defaultVersionSet + "1", PropertyGroupReference.builder(defaultPropertyGroupName, "1.0.0").build(validator));
		createVersionSet(defaultVersionSet + "2", PropertyGroupReference.builder(defaultPropertyGroupName, "1.0.0").build(validator));
		
		propertyGroupRepository.delete(expectedPropertyGroup.getName(), expectedPropertyGroup.getVersion());
		
	}

	/**
	 * Tests the retrieval of versions of a {@link PropertyGroup} by creating some versions and then checking that 
	 * all of them are listed.
	 */
	@Test
	public void testVersions() {
	    
	    final Set<String> expectedVersions = Sets.newHashSet("1.0.0","1.0.1");
	    
        expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
	        .withDescription("property-group-1-description")
	        .withType(PropertyGroupType.APP.name())
	        .withIsActive(true)
	        .withCreatedBy("Wagner Y. Matsushita")
	        .withCreatedDate(LocalDateTime.now())
	        .withProperties(property1, property2)
	        .build(validator);
        propertyGroupRepository.create(expectedPropertyGroup);
	    
        expectedPropertyGroup = PropertyGroup.builder(defaultPropertyGroupName, "1.0.1")
            .withDescription("property-group-1-description")
            .withType(PropertyGroupType.APP.name())
            .withIsActive(true)
            .withCreatedBy("Wagner Y. Matsushita")
            .withCreatedDate(LocalDateTime.now())
            .withProperties(property1, property2)
            .build(validator);
        
	    try {
	        propertyGroupRepository.update(defaultPropertyGroupName, expectedPropertyGroup);
	        
	        final Set<String> versions = propertyGroupRepository.versions(defaultPropertyGroupName).get();
	        assertThat(versions).isEqualTo(expectedVersions);
	        
	        LOGGER.debug("versions: " + versions.toString());
            LOGGER.debug("expectedVersions: " + expectedVersions.toString());	        
	        
	    } catch (Throwable t) {
	        LOGGER.error("Error while trying to fetch versions of a PropertyGroup", t);
	        fail("Error while trying to fetch versions of a PropertyGroup");
	    }
	}
	
	private void createPropertyGroup(final String propertyGroupName, final String propertyGroupVersion ) {
		
	    expectedPropertyGroup = PropertyGroup.builder(propertyGroupName, propertyGroupVersion)
        		.withDescription("property-group-1-description")
        		.withType(PropertyGroupType.APP.name())
        		.withIsActive(true)
        		.withCreatedBy("Igor K. Shiohara")
        		.withCreatedDate(LocalDateTime.now())
        		.withProperties(property1, property2)
        		.build(validator);
        		
		propertyGroupRepository.create(expectedPropertyGroup);
	}

	private void createVersionSet(final String versionSetName, final PropertyGroupReference...  pgReference) {
	    
	    final VersionSet versionSet = VersionSet.builder(versionSetName, "1.0.0")
                .withDescription("test-version-set-1-description")
                .withCreatedBy("Igor K. Shiohara")
                .withCreatedDate(LocalDateTime.now())
                .withPropertyGroupReferences(pgReference)
                .build(validator);       

        versionSetRepository.create(versionSet);
	}

}
