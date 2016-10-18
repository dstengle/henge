package com.kenzan.henge.repository.impl.flatfile.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.config.RandomValuePropertySource;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SerializationUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.exception.HengeValidationException;


/**
 * Unit test class for the {@link S3FileStorageServiceTest} class.
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","flatfile_s3","setmapping"})
public class S3FileStorageServiceTest {

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private AmazonS3 amazonS3;
    
    @Value("${repository.bucket.name}")
    private String bucketName;
    
    @Value("${text.encoding}")
    private String textEncoding;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private String randomToken;
    private String defaultTestFileName;
    private String defaultPropertyGroupName;
    
    public S3FileStorageServiceTest() {
        randomToken = "-"+ new Random().nextInt(1000000-1)+1;
        defaultTestFileName = "test-file-" + randomToken;
        defaultPropertyGroupName = "test-property-group" + randomToken;
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        
        fileStorageService.deleteBeginningWith(StringUtils.EMPTY, defaultTestFileName);
        fileStorageService.deleteBeginningWith(StringUtils.EMPTY, defaultPropertyGroupName);
    }

    @Test
    public void testWrite() throws Exception {

        final String expectedText = "Text content of file";
        
        fileStorageService.write(StringUtils.EMPTY, defaultTestFileName, expectedText, false);
        
        assertTrue(amazonS3.doesObjectExist(bucketName, defaultTestFileName));
        
        final String text = new String(IOUtils.toByteArray(amazonS3.getObject(bucketName, defaultTestFileName).getObjectContent()), Charset.forName(textEncoding));
        assertEquals(expectedText, text);
        
        //this should be allowed with no exception thrown
        fileStorageService.write(StringUtils.EMPTY, defaultTestFileName, expectedText, true);
        
        thrown.expect(HengeValidationException.class);
        thrown.expectMessage("The file cannot be written because it already exists.");
        //this should not be allowed because the file already exists and the overwrite flag is set to false
        fileStorageService.write(StringUtils.EMPTY, defaultTestFileName, expectedText, false);
        
    }

    @Test
    public void testWriteBytes() throws Exception {

        final Object expectedObject = PropertyGroup.builder(defaultPropertyGroupName, "1.0.0")
        .withCreatedBy("Wagner Yukio Matsushita")
        .withCreatedDate(LocalDateTime.now())
        .withDescription("Test Description")
        .withIsActive(true)
        .build();
        
        final byte[] expectedBytes = SerializationUtils.serialize(expectedObject);
        
        fileStorageService.writeBytes(StringUtils.EMPTY, defaultTestFileName, expectedBytes, false);
        
        assertTrue(amazonS3.doesObjectExist(bucketName, defaultTestFileName ));
        
        final byte[] bytes =  IOUtils.toByteArray(amazonS3.getObject(bucketName, defaultTestFileName).getObjectContent());
        assertTrue(Arrays.equals(expectedBytes, bytes));
        
        final Object objectRead = SerializationUtils.deserialize(bytes);
        assertEquals(expectedObject, objectRead);
        
        //this should be allowed with no exception thrown
        fileStorageService.writeBytes(StringUtils.EMPTY, defaultTestFileName, SerializationUtils.serialize(expectedObject), true);
        
        thrown.expect(HengeValidationException.class);
        thrown.expectMessage("The file cannot be written because it already exists.");
        //this should not be allowed because the file already exists and the overwrite flag is set to false
        fileStorageService.writeBytes(StringUtils.EMPTY, defaultTestFileName, SerializationUtils.serialize(expectedObject), false);
        
    }

    @Test
    public void testRead() throws Exception {

        final String expectedText = "Text content of file";
        
        writeBytesToS3(defaultTestFileName, expectedText.getBytes(textEncoding));
        
        final String text = fileStorageService.read(StringUtils.EMPTY, defaultTestFileName).get();
        assertEquals(expectedText, text);
        
        final Optional<String> shouldBeAbsent = fileStorageService.read(StringUtils.EMPTY, "this-file-does-not-exist");
        assertFalse(shouldBeAbsent.isPresent());
        
    }

    @Test
    public void testReadBytes() throws Exception {

        final byte[] expectedBytes = new String("Text content of file").getBytes(textEncoding);
        
        writeBytesToS3(defaultTestFileName, expectedBytes);
        
        final byte[] bytes = fileStorageService.readBytes(StringUtils.EMPTY, defaultTestFileName).get();
        assertTrue(Arrays.equals(expectedBytes, bytes));
        
        Optional<byte[]> shouldBeAbsent = fileStorageService.readBytes(StringUtils.EMPTY, "this-file-does-not-exist");
        assertFalse(shouldBeAbsent.isPresent());
        
    }

    @Test
    public void testDelete() throws Exception {

        final String expectedText = "Text content of file";
        
        writeBytesToS3(defaultTestFileName, expectedText.getBytes(textEncoding));

        assertTrue(amazonS3.doesObjectExist(bucketName, defaultTestFileName));
        assertTrue(fileStorageService.delete(StringUtils.EMPTY, defaultTestFileName));
        assertFalse(amazonS3.doesObjectExist(bucketName, defaultTestFileName));
        
        assertFalse(fileStorageService.delete(StringUtils.EMPTY, "this-file-does-not-exist"));
        
    }

    @Test
    public void testDeleteBeginningWith() throws Exception {

        final String expectedText = "Text content of file";
        
        writeBytesToS3(defaultTestFileName, expectedText.getBytes(textEncoding));
        
        assertTrue(amazonS3.doesObjectExist(bucketName, defaultTestFileName));
        List<String> deletedFiles = fileStorageService.deleteBeginningWith(StringUtils.EMPTY, defaultTestFileName);
        assertTrue(deletedFiles.contains(defaultTestFileName));
        assertFalse(amazonS3.doesObjectExist(bucketName, defaultTestFileName));
        
        deletedFiles = fileStorageService.deleteBeginningWith(StringUtils.EMPTY, "this-file-does-not-exist");
        assertTrue(deletedFiles.size() == 0);

    }

    @Test
    public void testExists() throws Exception {

        final String expectedText = "Text content of file";
        
        writeBytesToS3(defaultTestFileName, expectedText.getBytes(textEncoding));
        
        assertEquals(amazonS3.doesObjectExist(bucketName, defaultTestFileName), fileStorageService.exists(StringUtils.EMPTY, defaultTestFileName));
        
        amazonS3.deleteObject(bucketName, defaultTestFileName);
        
        assertEquals(amazonS3.doesObjectExist(bucketName, defaultTestFileName), fileStorageService.exists(StringUtils.EMPTY, defaultTestFileName));

    }

    @Test
    public void testExistsBeginningWith() throws Exception {
        
        final String expectedText = "Text content of file";
        
        writeBytesToS3(defaultTestFileName, expectedText.getBytes(textEncoding));
        
        assertTrue(fileStorageService.existsBeginningWith(StringUtils.EMPTY, defaultTestFileName));
        
        amazonS3.deleteObject(bucketName, defaultTestFileName);
        
        assertFalse(fileStorageService.existsBeginningWith(StringUtils.EMPTY, defaultTestFileName));

    }

    @Test
    public void testGetFileNamesStartingWith() throws Exception {
        
        final String expectedText = "Text content of file";
        final List<String> expectedNames = new ArrayList<String>();
        
        for (int i=0; i<5; i++) {
            expectedNames.add(defaultTestFileName + i);
            writeBytesToS3(defaultTestFileName + i, expectedText.getBytes(textEncoding));
        }
        
        Optional<Set<String>> optFileNames = fileStorageService.getFileNamesStartingWith(StringUtils.EMPTY, "best-file");
        assertFalse(optFileNames.isPresent());
        
        Set<String> fileNames = fileStorageService.getFileNamesStartingWith(StringUtils.EMPTY, defaultTestFileName).get();
        assertTrue(fileNames.size() == 5);
        
        for (int i=0; i<5; i++) {
            assertTrue(fileNames.contains(expectedNames.get(i)));
        }
        
    }
    
    private void writeBytesToS3(String fileName, byte[] data) {
        final InputStream inputStream = new ByteArrayInputStream(data);
        
        final ObjectMetadata objectMetaData = new ObjectMetadata();
        objectMetaData.setContentLength(data.length);
        
        final PutObjectRequest putReq = new PutObjectRequest(bucketName, fileName, inputStream, objectMetaData);

        amazonS3.putObject(putReq);
    }

}
