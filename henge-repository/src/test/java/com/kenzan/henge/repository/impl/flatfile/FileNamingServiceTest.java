package com.kenzan.henge.repository.impl.flatfile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for the {@link FileNamingService} class. 
 * The tests here assume the naming rules defined there. 
 * If those rules change, the tests must be changed too.
 * 
 * @author wmatsushita
 */
public class FileNamingServiceTest {

	private static String FILE_NAME_SEPARATOR = FileNamingService.FILE_NAME_SEPARATOR;
	
    private FileNamingService fileNamingService;

    /**
     * 
     */
    public FileNamingServiceTest() {

        fileNamingService = new FileNamingService();

    }

    @Test
    public void testGetCompleteFileName() {

        final String expectedFileName = "test-name" + FILE_NAME_SEPARATOR + "test-version";
        final String name = fileNamingService.getCompleteFileName("test-name", "test-version");

        assertEquals(expectedFileName, name);

    }

    @Test
    public void testGetFileNameFromEntityTypeAndCompleteName() {

        final String expectedFileName = "test-name" + FILE_NAME_SEPARATOR;
        final String name = fileNamingService.getFileName("test-name");

        assertEquals(expectedFileName, name);

    }
    
    @Test
    public void testExtractVersionFromFileName() {

        final String expectedVersion = "test-version";
        String version = fileNamingService.extractEntityVersionFromFileName("test-name" + FILE_NAME_SEPARATOR + "test-version");
        assertEquals(expectedVersion, version);
        
        version = fileNamingService.extractEntityVersionFromFileName("test-name" + FILE_NAME_SEPARATOR + "WithSeparatorInTheName" + FILE_NAME_SEPARATOR + "test-version");
        assertEquals(expectedVersion, version);
        

    }

    @Test
    public void testExtractNameFromFileName() {

        String expectedName = "test-name";
        String name = fileNamingService.extractEntityNameFromFileName("test-name" + FILE_NAME_SEPARATOR + "test-version");
        assertEquals(expectedName, name);
        
        expectedName = "PropertyGroup" + FILE_NAME_SEPARATOR + "WithSeparatorInTheName";
        name = fileNamingService.extractEntityNameFromFileName(expectedName + FILE_NAME_SEPARATOR + "some-version");
        assertEquals(expectedName, name);

    }
}
