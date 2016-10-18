package com.kenzan.henge.resource.v1.integration;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.FileVersionReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.service.FileBD;
import com.kenzan.henge.util.AbstractIntegrationTest;
import com.kenzan.henge.util.CleanerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Igor K. Shiohara
 */
@TestPropertySource(properties = {"multipart.max-file-size", "10Mb"}) //It was necessary to change because the RestAssured is not working properly, stopping execution when the Tomcat Exception throws the FileSizeLimitExceededException exception
public class FileRSIT extends AbstractIntegrationTest {

    @Autowired
    private VersionSetRepository versionSetRepository;
    
	@Autowired
	private FileBD fileService;
	private FileVersion fileVersion1, fileVersion2;

	private File file1 = new File("src/test/resources/files/test1.txt");
	@Autowired
	private CleanerUtils cleanerUtils;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void init() {
		fileVersion1 = FileVersion.builder("FileVersion-Test1", "1.0.0", "File Content".getBytes(), "File1-filename.txt")
				.withDescription("File description").build();
		fileVersion2 = FileVersion.builder("FileVersion-Test1", "1.0.1", "File Content".getBytes(), "File1-filename.txt")
				.withDescription("File description").build();
		fileService.create(fileVersion1);
		fileService.update("FileVersion-Test1", fileVersion2);
	}
	
	/**
	 * Tests the file upload
	 */
	@Test
	public void uploadFileTest() {
		
		String jsonRequest = "{\"name\": \"FileVersion-Test2\", \"version\": \"1.0.0\", \"description\": \"Description\", \"createdDate\":null }";
		
		given().auth().basic("user", "user")
								.multiPart(file1)
								.formParam("data", jsonRequest)
								.expect()
									.statusCode(200)
									.body("name", Matchers.equalTo("FileVersion-Test2"))
									.body("version", Matchers.equalTo("1.0.0"))
									.body("description", Matchers.equalTo("Description"))
									.body("filename", Matchers.equalTo("test1.txt"))
								.when().post("/henge/v1/files/upload");
	}
	
	/**
	 * Tests the 2mb size validation
	 * @throws IOException 
	 */
	@SuppressWarnings("resource")
	@Test
	public void maxSizeValidationTest() throws IOException {
		File fileMoreThan2mbSize = new File("src/test/resources/files/FileVersion_2mb_size_validation");
		
		final String mode_read_and_write = "rw";
		new RandomAccessFile(fileMoreThan2mbSize, mode_read_and_write).setLength(1024 * 1024 * 3);
		
		
		String jsonRequest = "{\"name\": \"FileVersion-Test_2mb_size_validation\", \"version\": \"1.0.0\", \"description\": \"Too large file\", \"createdDate\":null }";
		
		Response response = given().auth().basic("user", "user")
								.multiPart(fileMoreThan2mbSize)
								.formParam("data", jsonRequest)
								.when().post("/henge/v1/files/upload");
		assertEquals(500, response.getStatusCode());
		assertEquals("The field file exceeds its maximum permitted size of 2097152 bytes.", response.getBody().asString());
		
	}
	
	/**
	 * Tests update a file
	 * @throws IOException 
	 */
	@Test
	public void updateFileTest() throws IOException {
		final byte[] bytes = IOUtils.toByteArray(new FileInputStream(file1));
		FileVersion old = FileVersion.builder("FileVersion-Test-Update","1.0.0", bytes , "test1.txt").withDescription("Old description").build();
		fileService.create(old);
		String jsonRequest = "{\"name\": \"FileVersion-Test-Update\", \"version\": \"1.0.1\", \"description\": \"New Description\", \"createdDate\":null }";
		
		given().auth().basic("user", "user")
								.multiPart(new File("src/test/resources/files/test2.txt"))
								.formParam("data", jsonRequest)
								.expect()
									.statusCode(200)
									.body("name", Matchers.equalTo("FileVersion-Test-Update"))
									.body("version", Matchers.equalTo("1.0.1"))
									.body("description", Matchers.equalTo("New Description"))
									.body("filename", Matchers.equalTo("test2.txt"))
								.when().put("/henge/v1/files/update");	
	}
	
	/**
	 * Download a file by name test
	 */
	@Test
	public void downloadByNameSuccessfulTest() {
		given().auth().basic("user", "user")
		.with().contentType(ContentType.BINARY)
		.then().expect()
			.statusCode(200)
			.body(equalTo("File Content"))
			.when().get("/henge/v1/files/FileVersion-Test1");
	}
	
	/**
	 * Download a file by name and version test
	 */
	@Test
	public void downloadByNameAndVersionSuccessfulTest() {
		given().auth().basic("user", "user")
		.with().contentType(ContentType.BINARY)
		.then().expect()
			.statusCode(200)
			.body(equalTo("File Content"))
			.when().get("/henge/v1/files/FileVersion-Test1/versions/1.0.0");
	}
	
	/**
	 * Download a non existent file by name test
	 */
	@Test
	public void downloadNonExistentFileByNameTest() {
		Response response = given().auth().basic("user", "user")
		.with().contentType(ContentType.BINARY)
		.when().get("/henge/v1/files/NonExistentFileVersionName");
		
		assertEquals(404, response.getStatusCode());
		assertEquals("No FileVersion was found by the given name [NonExistentFileVersionName].", response.getBody().asString());
	}
	
	/**
	 * Download a non existent file by name and version test
	 */
	@Test
	public void downloadNonExistentFileByNameAndVersionTest() {
		Response response = given().auth().basic("user", "user")
		.with().contentType(ContentType.BINARY)
		.when().get("/henge/v1/files/NonExistentFileVersionName/versions/1.0.0");
		
		assertEquals(404, response.getStatusCode());
		assertEquals("No FileVersion was found by the given name [NonExistentFileVersionName] and version [1.0.0].", response.getBody().asString());
	}
	
	/**
     * Tests the retrieving of the latest version number of a specific {@link FileVersion}
     */
    @Test
    public void latestVersionNumberTest() {
        
        final Response response = given().auth().basic("user", "user").when().get("/henge/v1/files/FileVersion-Test1/versions/ceiling");
        assertEquals(200, response.getStatusCode());
        assertEquals("1.0.1", response.getBody().asString());
        
    }
    
    /**
     * Tests the retrieving of the latest version number of a specific {@link FileVersion}
     */
    @Test
    public void nonExistsLatestFileVersionTest() {
        
        final Response response = given().auth().basic("user", "user").when().get("/henge/v1/files/NonExistentFileVersion/versions/ceiling");
        assertEquals(404, response.getStatusCode());
        assertEquals("No FileVersion was found by the given name [NonExistentFileVersion].", response.getBody().asString());
        
    }
    
    /**
     * Test get all versions endpoint, to return all {@link FileVersion} version numbers by name
     */
    @Test
    public void allFileVersionVersionsTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/files/FileVersion-Test1/versions");
        
        assertEquals(200, response.getStatusCode());
        assertEquals(2, response.getBody().as(Set.class).size());
    }
    
    /**
     * Test the validation with a non existent FileVersion to get the versions
     */
    @Test
    public void nonExistentFileVersionValidationTest() {
        
        final Response response = given().auth().basic("user", "user")
        .when().get("/henge/v1/files/UnknownFileVersion/versions");
        
        assertEquals(404, response.getStatusCode());
        assertEquals("No FileVersion was found by the given name [UnknownFileVersion].", response.getBody().asString());
    }

	/**
	 * Deletion test by name only
	 */
	@Test
	public void deleteSuccessTest() {
		given().auth().basic("user", "user").with().content(ContentType.JSON).then().expect().statusCode(200).when()
				.delete("/henge/v1/files/FileVersion-Test1");
	}

	/**
	 * Deletion test by name and version
	 */
	@Test
	public void deleteByVersionSuccessTest() {
		given().auth().basic("user", "user").with().content(ContentType.JSON).then().expect().statusCode(200).when()
				.delete("/henge/v1/files/FileVersion-Test1/versions/1.0.0");
	}

	/**
	 * Deletion a non existent {@link FileVersion} test by name only
	 */
	@Test
	public void deleteNonExistentByNameTest() {
		Response response = given().auth().basic("user", "user").with().content(ContentType.JSON).when()
				.delete("/henge/v1/files/NonExistentFileVersionName");
		
		assertEquals(404, response.getStatusCode());
		assertEquals("No FileVersion was found by the given name [NonExistentFileVersionName].", response.getBody().asString());
	}

	/**
	 * Deletion a non existent {@link FileVersion} test by name and version
	 */
	@Test
	public void deleteNonExistentByNameAndVersionTest() {
		Response response = given().auth().basic("user", "user").with().content(ContentType.JSON).when()
				.delete("/henge/v1/files/NonExistentFileVersionName/versions/1.0.0");
		
		assertEquals(404, response.getStatusCode());
		assertEquals("No FileVersion was found by the given name [NonExistentFileVersionName] and version [1.0.0] to be deleted.", response.getBody().asString());
	}
	
	@Test
	public void downloadFromVersionSetTest(){
	    
	    versionSetRepository.create(VersionSet.builder("VersionSet-Test", "1.0.0")
            .withDescription("VersionSet App description")
            .withCreatedBy("Marcos Senandes Simon")
            .withFileVersionReferences(FileVersionReference.builder(fileVersion1).build())
            .withPropertyGroupReferences()
        .build());
	    
	    given().auth().basic("user", "user")
        .with().contentType(ContentType.BINARY)
        .then().expect()
            .statusCode(200)
            .body(equalTo("File Content"))
            .when().get("/henge/v1/files/VersionSet-Test/1.0.0/FileVersion-Test1");
	}
	    
    @Test
	public void setAUnexistentFileVersionAsCurrentVersionTest() {
		expectedException.expect(HengeValidationException.class);
		expectedException.expectMessage("This FileVersion with name UnexistentFileVersion and version 1.0.0 doesn't exists.");
		
		fileService.setCurrentVersion("UnexistentFileVersion", "1.0.0");
	}

	@After
	public void tearDown() {

		cleanerUtils.execute();

	}

}
