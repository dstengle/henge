package com.kenzan.henge.domain.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kenzan.henge.domain.AbstractBaseDomainTest;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Unit tests for the {@link FileVersion} model class
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class FileVersionTest extends AbstractBaseDomainTest {

    private FileVersion expectedFileVersion;
    private LocalDateTime testDateTime;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        //Fixes a creation date to be consistent throughout the test
        testDateTime = LocalDateTime.of(1980,10, 9, 7, 20);
        expectedFileVersion = FileVersion.builder("TestFileVersion", "1.0.0", "Test file Content".getBytes(), "test.txt")
                        .withCreatedBy("Wagner Y. Matsushita")
                        .withCreatedDate(testDateTime)
                        .withDescription("Test FileVersion description")
                        .build();
    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.FileVersion#hashCode()}.
     */
    @Test
    public void testHashCode() {
        final FileVersion copy = FileVersion.builder(expectedFileVersion).build();
        
        assertTrue(copy.hashCode() == expectedFileVersion.hashCode());
        assertTrue(copy.equals(expectedFileVersion) && expectedFileVersion.equals(copy));
        
        FileVersion changed = FileVersion.builder(expectedFileVersion).withVersion("1.0.1").build();
        assertFalse(changed.hashCode() == expectedFileVersion.hashCode());
        assertFalse(changed.equals(expectedFileVersion) && expectedFileVersion.equals(changed));
        
        changed = FileVersion.builder(expectedFileVersion).withDescription("different description").build();
        assertFalse(changed.hashCode() == expectedFileVersion.hashCode());
        assertFalse(changed.equals(expectedFileVersion) && expectedFileVersion.equals(changed));

        changed = FileVersion.builder(expectedFileVersion).withContent("different content".getBytes()).build();
        assertFalse(changed.hashCode() == expectedFileVersion.hashCode());
        assertFalse(changed.equals(expectedFileVersion) && expectedFileVersion.equals(changed));
        
        changed = FileVersion.builder(expectedFileVersion).withFileName("different fileName").build();
        assertFalse(changed.hashCode() == expectedFileVersion.hashCode());
        assertFalse(changed.equals(expectedFileVersion) && expectedFileVersion.equals(changed));
        
    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.FileVersion#equals(java.lang.Object)}.
     */
    @Test
    public void testCopyBuilderAndEquals() {

        FileVersion copy = FileVersion.builder(expectedFileVersion).build();
        
        assertThat(copy, equalTo(expectedFileVersion));
        
        assertThat(copy.getName(), equalTo(expectedFileVersion.getName()));
        assertThat(copy.getVersion(), equalTo(expectedFileVersion.getVersion()));
        assertThat(copy.getDescription(), equalTo(expectedFileVersion.getDescription()));
        assertThat(copy.getFilename(), equalTo(expectedFileVersion.getFilename()));
        assertThat(copy.getContent(), equalTo(expectedFileVersion.getContent()));
        
    }
    
    @Test
    public void testJsonSerialization() throws Exception {
        final String expectedJson = "{\"name\":\"TestFileVersion\",\"version\":\"1.0.0\",\"description\":\"Test FileVersion description\",\"content\":\"VGVzdCBmaWxlIENvbnRlbnQ=\",\"filename\":\"test.txt\",\"createdBy\":\"Wagner Y. Matsushita\",\"createdDate\":\"1980-10-09T07:20:00\"}";        

        final String json = mapper.writeValueAsString(expectedFileVersion);
        
        assertThat(json, equalTo(expectedJson));
        
    }
    
    @Test
    public void testJsonDeserialization() throws Exception {
        final String json = "{\"name\":\"TestFileVersion\",\"version\":\"1.0.0\",\"description\":\"Test FileVersion description\",\"content\":\"VGVzdCBmaWxlIENvbnRlbnQ=\",\"filename\":\"test.txt\",\"createdBy\":\"Wagner Y. Matsushita\",\"createdDate\":\"1980-10-09T07:20\"}";        

        final FileVersion fileVersion = mapper.readValue(json, FileVersion.class);
        
        assertThat(fileVersion, equalTo(expectedFileVersion));
        
    }

}
