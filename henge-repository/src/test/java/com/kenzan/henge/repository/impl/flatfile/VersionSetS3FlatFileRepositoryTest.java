package com.kenzan.henge.repository.impl.flatfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.Sets;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.exception.HengeValidationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlProcessor.ResolutionMethod;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class provides tests for the flat file implementation of the CRUD of VersionSets.
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","flatfile_s3","setmapping"})
public class VersionSetS3FlatFileRepositoryTest extends AbstractVersionSetFlatFileRepositoryTest {
    
    @Value("${repository.bucket.name}")
    private String bucketName;
    
    @Value("${text.encoding}")
    private String textEncoding;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetS3FlatFileRepositoryTest.class);
    
    public VersionSetS3FlatFileRepositoryTest() {
        randomToken = "-"+ new Random().nextInt(1000000-1)+1;
        defaultVersionSet = "test-version-set" + randomToken;
        defaultPropertyGroupName = "test-property-group" + randomToken;
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
                .build(validator);       

        versionSetRepository.create(expectedVersionSet);
        
        final String content = jsonUtils.toJson(versionSetRepository.read(expectedVersionSet.getName(), expectedVersionSet.getVersion()).get());

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
        .build(validator);       

        versionSetRepository.create(expectedVersionSet);
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.1")
        .withDescription("version-set-1-description")
        .withCreatedBy("Wagner Y. Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withPropertyGroupReferences(pgReference1, pgReference2)
        .build(validator);       

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
                .build(validator);       

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
                .build(validator);       

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
            .build(validator);
    
        versionSetRepository.create(expectedVersionSet);
        
        VersionSet versionSet = VersionSet.builder(expectedVersionSet).withVersion("1.0.1")
        		.build();
                    
        versionSetRepository.update(defaultVersionSet, versionSet);
        
        assertThat(versionSet.getVersion()).isEqualTo("1.0.1");
            
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
        .build(validator);
        
        versionSetRepository.create(expectedVersionSet);
        
        assertThat(fileStorageService.exists(fileNamingService.getPath(VersionSet.class),fileNamingService.getCompleteFileName(expectedVersionSet.getName(), expectedVersionSet.getVersion()))).isTrue();
        
        versionSetRepository.delete(expectedVersionSet.getName(), expectedVersionSet.getVersion());
        
        assertThat(fileStorageService.exists(fileNamingService.getPath(VersionSet.class), fileNamingService.getCompleteFileName(expectedVersionSet.getName(), expectedVersionSet.getVersion()))).isFalse();
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
            .build(validator);
        versionSetRepository.create(expectedVersionSet);
        
        expectedVersionSet = VersionSet.builder(defaultVersionSet, "1.0.1")
            .withDescription("version-set-1-description")
            .withCreatedBy("Wagner Y. Matsushita")
            .withCreatedDate(LocalDateTime.now())
            .withPropertyGroupReferences(pgReference1, pgReference2)
            .build(validator);
        
        try {
            versionSetRepository.update(defaultVersionSet, expectedVersionSet);
            
            final Set<String> versions = versionSetRepository.versions(defaultVersionSet).get();
            assertThat(versions).isEqualTo(expectedVersions);
            
        } catch (Throwable t) {
            LOGGER.error("Error while trying to fetch versions of a VersionSet", t);
            fail("Error while trying to fetch versions of a VersionSet");
        }
    }
    
    @Configuration
    @ComponentScan("com.kenzan.henge")
    public static class TestConfig {
        
        @Bean
        public static PropertySourcesPlaceholderConfigurer properties() throws Exception {
            final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();

            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
            yaml.setResolutionMethod(ResolutionMethod.OVERRIDE);
            yaml.setResources(new ClassPathResource("application.yml"), new ClassPathResource("application-flatfile_s3.yml"));
            pspc.setProperties(yaml.getObject());           
            
            return pspc;
        }

    }   

}
