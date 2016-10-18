package com.kenzan.henge.repository.impl.flatfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.Sets;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.FileVersionReference;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.FileVersionRepository;
import com.kenzan.henge.repository.PropertyGroupRepository;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
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
 * This class provides tests for the flat file implementation of the CRUD of VersionSets.
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","flatfile_local","setmapping"})
public class VersionSetFlatFileRepositoryTest extends AbstractVersionSetFlatFileRepositoryTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetFlatFileRepositoryTest.class);
    
    @Value("${user.home}${repository.location}")
    private String repositoryLocation;
    
    @Value("${text.encoding}")
    private String textEncoding;
    
    private VersionSet expectedVersionSet;
    private PropertyGroupReference pgReference1, pgReference2;
    private FileVersionReference fileVersionReference1, fileVersionReference2;
    private String defaultFileVersionName;
    
    @Autowired
    private PropertyGroupRepository propertyGroupRepository;
    
    @Autowired
    private FileVersionRepository fileVersionRepository;
    
    public VersionSetFlatFileRepositoryTest() {
        randomToken = "-"+ new Random().nextInt(1000000-1)+1;
        defaultPropertyGroupName = "test-property-group" + randomToken;
        defaultFileVersionName = "test-file-version" + randomToken;
        
        defaultVersionSet = "test-version-set" + randomToken;
    }
    
    
    /**
     * Setting up properties to fill the VersionSet object. It's the same ones for all of them.
     */
    @Before
    public void setUp() {
    	propertyGroupRepository.create(PropertyGroup.builder(defaultPropertyGroupName + "1", "1.0.0").build());
    	propertyGroupRepository.create(PropertyGroup.builder(defaultPropertyGroupName + "2", "1.0.0").build());
    	
    	fileVersionRepository.create(FileVersion.builder(defaultFileVersionName + "1", "1.0.0", "Dummy Content1".getBytes(), "dummy_file1.txt").build(validator));
        fileVersionRepository.create(FileVersion.builder(defaultFileVersionName + "2", "1.0.0", "Dummy Content2".getBytes(), "dummy_file2.txt").build(validator));
    	
        pgReference1 = PropertyGroupReference.builder(defaultPropertyGroupName + "1", "1.0.0").build();
        pgReference2 = PropertyGroupReference.builder(defaultPropertyGroupName + "2", "1.0.0").build();
        
        fileVersionReference1 = FileVersionReference.builder(defaultFileVersionName + "1", "1.0.0").build();
		fileVersionReference2 = FileVersionReference.builder(defaultFileVersionName + "2", "1.0.0").build();
    }
    
    /**
     * Tests creation of a new {@link VersionSet} by attempting to create it and then read it's contents and 
     * comparing for equality to the json serialized from the same VersionSet instance used to generate the file
     */
    @Test
    public void testCreateNewVersionSet() throws Exception {
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.0")
                .withDescription("test-version-set-1-description")
                .withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.now())
                .withPropertyGroupReferences(pgReference1, pgReference2)
                .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
                .build();       

        versionSetRepository.create(expectedVersionSet);
        
        final String content = new String(
                Files.readAllBytes(
                        FileSystems.getDefault().getPath(repositoryLocation,
                            fileNamingService.getPath(expectedVersionSet.getClass()),
                            fileNamingService.getCompleteFileName(expectedVersionSet.getName(), "1.0.0"))
                ), Charset.forName(textEncoding)
        ).trim();
        
        LOGGER.debug("Content: " + content);
        assertThat(content).isEqualTo(jsonUtils.toJson(VersionSet.builder(expectedVersionSet).build(validator)));
            
    }
    
    /**
     * Test that when attempting to create a {@link VersionSet} that already has an existing version, 
     * the proper exception is thrown
     */
    @Test
    public void testCreateAlreadyExistingVersionSet() {
        
        exception.expect(HengeValidationException.class);
        exception.expectMessage("The VersionSet being created already exists. Consider using the update method.");
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.0")
        .withDescription("version-set-1-description")
        .withCreatedBy("Wagner Y. Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withPropertyGroupReferences(pgReference1, pgReference2)
        .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
        .build();       

        versionSetRepository.create(expectedVersionSet);
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.1")
        .withDescription("version-set-1-description")
        .withCreatedBy("Wagner Y. Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withPropertyGroupReferences(pgReference1, pgReference2)
        .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
        .build();       

        versionSetRepository.create(expectedVersionSet);
        
    }
        
    /**
     * Saves an expected VersionSet object, reads it back and checks for equality
     */
    @Test
    public void testReadFile() {
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.0")
                .withDescription("version-set-1-description")
                .withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.now())
                .withPropertyGroupReferences(pgReference1, pgReference2)
                .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
                .build();       

        versionSetRepository.create(expectedVersionSet);
        
        final VersionSet versionSet = versionSetRepository.read(expectedVersionSet.getName(), expectedVersionSet.getVersion()).get();
        assertThat(versionSet).isEqualTo(expectedVersionSet);
            
        
    }
    
    /**
     * Tests that an attempt to read a non existing {@link VersionSet} fails in the right way, 
     * by throwing {@link HengeResourceNotFoundException}.
     */
    @Test
    public void testReadNonExistingFile() {

        final Optional<VersionSet> entity = versionSetRepository.read("this name does not exist", "this version does not exist");
        assertThat(entity.isPresent()).isFalse();
        
    }
    
    /**
     * Tries to update a non existing VersionSet and checks that the Exception thrown is the right one. 
     */
    @Test
    public void testUpdateNonExistingFile() {
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.0")
                .withDescription("version-set-1-description")
                .withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.now())
                .withPropertyGroupReferences(pgReference1, pgReference2)
                .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
                .build();       

        final Optional<VersionSet> entity = versionSetRepository.update(defaultVersionSet, expectedVersionSet);
        assertThat(entity.isPresent()).isFalse();
            
    }
    
    /**
     * Creates the VersionSet and tries to update it then checks that it was updated. 
     */
    @Test
    public void testCorrectUpdate() throws Exception {
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.0")
            .withDescription("version-set-1-description")
            .withCreatedBy("Wagner Y. Matsushita")
            .withCreatedDate(LocalDateTime.now())
            .withPropertyGroupReferences(pgReference1, pgReference2)
            .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
            .build();
    
        versionSetRepository.create(expectedVersionSet);
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.1")
            .withDescription("version-set-1-description")
            .withCreatedBy("Wagner Y. Matsushita")
            .withCreatedDate(LocalDateTime.now())
            .withPropertyGroupReferences(pgReference1, pgReference2)
            .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
            .build();
    
        versionSetRepository.update(defaultVersionSet, expectedVersionSet);
        
        String content = new String(
                Files.readAllBytes(
                        FileSystems.getDefault().getPath(repositoryLocation,
                            fileNamingService.getPath(expectedVersionSet.getClass()),
                            fileNamingService.getCompleteFileName(expectedVersionSet.getName(), expectedVersionSet.getVersion()))
                ), Charset.forName(textEncoding)
        ).trim();
        
        LOGGER.debug("Content: " + content);
        assertThat(content).isEqualTo(jsonUtils.toJson(
            VersionSet.builder(expectedVersionSet).build(validator)));
            
    }
    
    /**
     * Tests deletion of a {@link VersionSet} by passing a non existing name and version. 
     * The expected behavior is to throw a {@link HengeResourceNotFoundException}.
     */
    @Test
    public void testDeleteNonExistentFile() {
        
        Optional<VersionSet> entity = versionSetRepository.delete("This name does not exist", "This version does not exist");
        assertThat(entity.isPresent()).isFalse();
        
    }
    
    /**
     * Tests deletion of a {@link VersionSet} by creating a new one, checking that the file exists 
     * and then deleting it and checking that the file no longer exists.
     */
    @Test
    public void testDelete() {
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.0")
        .withDescription("version-set-1-description")
        .withCreatedBy("Wagner Y. Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withPropertyGroupReferences(pgReference1, pgReference2)
        .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
        .build();
        
        versionSetRepository.create(expectedVersionSet);
        
        Path path = FileSystems.getDefault().getPath(repositoryLocation,
                fileNamingService.getPath(expectedVersionSet.getClass()),
                fileNamingService.getCompleteFileName(expectedVersionSet.getName(), expectedVersionSet.getVersion()));
        assertThat(Files.exists(path)).isTrue();
        
        versionSetRepository.delete(expectedVersionSet.getName(), expectedVersionSet.getVersion());
        
        assertThat(Files.exists(path)).isFalse();
    }
    
    /**
     * Tests the retrieval of versions of a {@link VersionSet} by creating some versions and then checking that 
     * all of them are listed.
     */
    @Test
    public void testVersions() {
        
        final Set<String> expectedVersions = Sets.newHashSet("1.0.0","1.0.1");
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.0")
            .withDescription("version-set-1-description")
            .withCreatedBy("Wagner Y. Matsushita")
            .withCreatedDate(LocalDateTime.now())
            .withPropertyGroupReferences(pgReference1, pgReference2)
            .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
            .build();

        versionSetRepository.create(expectedVersionSet);
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.1")
            .withDescription("version-set-1-description")
            .withCreatedBy("Wagner Y. Matsushita")
            .withCreatedDate(LocalDateTime.now())
            .withPropertyGroupReferences(pgReference1, pgReference2)
            .withFileVersionReferences(fileVersionReference1, fileVersionReference2)
            .build();
        
        try {
            versionSetRepository.update(defaultVersionSet, expectedVersionSet);
            
            final Set<String> versions = versionSetRepository.versions(defaultVersionSet).get();
            assertThat(versions).isEqualTo(expectedVersions);
            
        } catch (Throwable t) {
            LOGGER.error("Error while trying to fetch versions of a VersionSet", t);
            fail("Error while trying to fetch versions of a VersionSet");
        }
    }

}
