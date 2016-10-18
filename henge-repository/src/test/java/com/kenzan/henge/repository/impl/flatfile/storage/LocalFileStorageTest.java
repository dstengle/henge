package com.kenzan.henge.repository.impl.flatfile.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kenzan.henge.config.JacksonConfig;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.impl.flatfile.storage.LocalFileStorageTest.TestConfig;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.util.SerializationUtils;


/**
 * Unit test class for the {@link LocalFileStorageTest} class.
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","flatfile_local","setmapping"})
public class LocalFileStorageTest {

    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${user.home}/${repository.location}")
    private String repositoryLocation;
    
    @Value("${text.encoding}")
    private String textEncoding;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        
        fileStorageService.deleteBeginningWith(StringUtils.EMPTY, "test-");

    }

    @Test
    public void testWrite() throws Exception {

        final String expectedText = "Text content of file";
        
        fileStorageService.write(StringUtils.EMPTY, "test-file", expectedText, false);
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        assertTrue(Files.exists(file));
        
        final String text = new String(Files.readAllBytes(file), Charset.forName(textEncoding));
        assertEquals(expectedText, text);
        
        //this should be allowed with no exception thrown
        fileStorageService.write(StringUtils.EMPTY, "test-file", expectedText, true);
        
        thrown.expect(HengeValidationException.class);
        thrown.expectMessage("The file cannot be created because it already exists.");
        //this should not be allowed because the file already exists and the overwrite flag is set to false
        fileStorageService.write(StringUtils.EMPTY, "test-file", expectedText, false);
        
    }

    @Test
    public void testWriteBytes() throws Exception {

        final Object expectedObject = PropertyGroup.builder("test", "1.0.0")
        .withCreatedBy("Wagner Yukio Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withDescription("Test Description")
        .withIsActive(true)
        .build();
        
        final byte[] expectedBytes = SerializationUtils.serialize(expectedObject);
        
        fileStorageService.writeBytes(StringUtils.EMPTY, "test-file", expectedBytes, false);
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        assertTrue(Files.exists(file));
        
        assertTrue(Arrays.equals(expectedBytes, Files.readAllBytes(file)));
        
        final Object objectRead = SerializationUtils.deserialize(Files.readAllBytes(file));
        assertEquals(expectedObject, objectRead);
        
        //this should be allowed with no exception thrown
        fileStorageService.writeBytes(StringUtils.EMPTY, "test-file", SerializationUtils.serialize(expectedObject), true);
        
        thrown.expect(HengeValidationException.class);
        thrown.expectMessage("The file cannot be created because it already exists.");
        //this should not be allowed because the file already exists and the overwrite flag is set to false
        fileStorageService.writeBytes(StringUtils.EMPTY, "test-file", SerializationUtils.serialize(expectedObject), false);
        
    }

    @Test
    public void testRead() throws Exception {

        final String expectedText = "Text content of file";
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        Files.write(file, expectedText.getBytes(textEncoding), StandardOpenOption.CREATE_NEW);
        
        final String text = fileStorageService.read(StringUtils.EMPTY, "test-file").get();
        assertEquals(expectedText, text);
        
        final Optional<String> shouldBeAbsent = fileStorageService.read(StringUtils.EMPTY, "this-file-does-not-exist");
        assertFalse(shouldBeAbsent.isPresent());
        
    }

    @Test
    public void testReadBytes() throws Exception {

        final byte[] expectedBytes = new String("Text content of file").getBytes(textEncoding);
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        Files.write(file, expectedBytes, StandardOpenOption.CREATE_NEW);
        
        final byte[] bytes = fileStorageService.readBytes(StringUtils.EMPTY, "test-file").get();
        assertTrue(Arrays.equals(expectedBytes, bytes));
        
        Optional<byte[]> shouldBeAbsent = fileStorageService.readBytes(StringUtils.EMPTY, "this-file-does-not-exist");
        assertFalse(shouldBeAbsent.isPresent());
        
    }

    @Test
    public void testDelete() throws Exception {

        final String expectedText = "Text content of file";
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        Files.write(file, expectedText.getBytes(textEncoding), StandardOpenOption.CREATE_NEW);
        
        assertTrue(Files.exists(file));
        assertTrue(fileStorageService.delete(StringUtils.EMPTY, "test-file"));
        assertFalse(Files.exists(file));
        
        assertFalse(fileStorageService.delete(StringUtils.EMPTY, "this-file-does-not-exist"));
        
    }

    @Test
    public void testDeleteBeginningWith() throws Exception {

        final String expectedText = "Text content of file";
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        Files.write(file, expectedText.getBytes(textEncoding), StandardOpenOption.CREATE_NEW);
        
        assertTrue(Files.exists(file));
        List<String> deletedFiles = fileStorageService.deleteBeginningWith(StringUtils.EMPTY, "test");
        assertTrue(deletedFiles.contains("test-file"));
        assertFalse(Files.exists(file));
        
        deletedFiles = fileStorageService.deleteBeginningWith(StringUtils.EMPTY, "this-file-does-not-exist");
        assertTrue(deletedFiles.size() == 0);

    }

    @Test
    public void testExists() throws Exception {

        final String expectedText = "Text content of file";
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        Files.write(file, expectedText.getBytes(textEncoding), StandardOpenOption.CREATE_NEW);
        
        assertEquals(Files.exists(file), fileStorageService.exists(StringUtils.EMPTY, "test-file"));
        
        Files.delete(file);
        
        assertEquals(Files.exists(file), fileStorageService.exists(StringUtils.EMPTY, "test-file"));

    }

    @Test
    public void testExistsBeginningWith() throws Exception {
        
        final String expectedText = "Text content of file";
        
        final Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file");
        Files.write(file, expectedText.getBytes(textEncoding), StandardOpenOption.CREATE_NEW);
        
        assertTrue(fileStorageService.existsBeginningWith(StringUtils.EMPTY, "tes"));
        
        Files.delete(file);
        
        assertFalse(fileStorageService.existsBeginningWith(StringUtils.EMPTY, "tes"));

    }

    @Test
    public void testGetFileNamesStartingWith() throws Exception {
        
        final String expectedText = "Text content of file";
        final List<String> expectedNames = new ArrayList<String>();
        
        for (int i=0; i<5; i++) {
            expectedNames.add("test-file" + i);
            Path file = FileSystems.getDefault().getPath(repositoryLocation, "test-file" + i);
            Files.write(file, expectedText.getBytes(textEncoding), StandardOpenOption.CREATE_NEW);
        }
        
        Optional<Set<String>> optFileNames = fileStorageService.getFileNamesStartingWith(StringUtils.EMPTY, "best-file");
        assertFalse(optFileNames.isPresent());
        
        Set<String> fileNames = fileStorageService.getFileNamesStartingWith(StringUtils.EMPTY, "test-file").get();
        assertTrue(fileNames.size() == 5);
        
        for (int i=0; i<5; i++) {
            assertTrue(fileNames.contains(expectedNames.get(i)));
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
            yaml.setResources(new ClassPathResource("application.yml"), new ClassPathResource("application-flatfile_local.yml"));
            pspc.setProperties(yaml.getObject());           
            
            return pspc;
        }

    }   
    

}
