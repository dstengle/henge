package com.kenzan.henge.repository.impl.flatfile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeIOException;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.exception.RuntimeHengeException;
import com.kenzan.henge.repository.FileVersionRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({ "dev", "flatfile_local", "setmapping" })
public class FileVersionFlatFileRepositoryIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileVersionFlatFileRepositoryIT.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private byte[] content1 = "Content file".getBytes(UTF_8);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private FileVersionRepository repository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileNamingService fileNamingService;

    @Autowired
    private JsonUtils jsonUtils;

    private String modelPath;
    private String dataPath;
    private FileVersion fileVersion;

    @Before
    public void setUp() {

        modelPath = fileNamingService.getPath(FileVersion.class);
        dataPath = fileNamingService.getPath(FileVersion.class, FileVersionFlatFileRepository.DATA_SUB_FOLDER_NAME);
        fileVersion =
            FileVersion.builder("Test__File1", "1.0.0", content1, "test_file.txt")
                .withDescription("File 1 description").withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.of(2016, 3, 22, 10, 45))
                .build();
    }

    /**
     * Successful creation test
     */
    @Test
    public void createTest() throws Exception {

        final FileVersion created = repository.create(fileVersion);
        assertNotNull(created);
        assertEquals("Test__File1", created.getName());
        assertEquals("Content file", new String(created.getContent(), UTF_8));

        final String fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.0");
        assertTrue(fileStorageService.exists(modelPath, fileName));

        final FileVersion createdModel =
            jsonUtils.fromJson(fileStorageService.read(modelPath, fileName).get(), FileVersion.class);
        assertEquals(fileVersion.getName(), createdModel.getName());
        assertEquals(fileVersion.getVersion(), createdModel.getVersion());
        assertEquals(fileVersion.getDescription(), createdModel.getDescription());
        assertEquals(fileVersion.getCreatedBy(), createdModel.getCreatedBy());
        assertEquals(fileVersion.getCreatedDate(), createdModel.getCreatedDate());

        assertTrue(fileStorageService.exists(dataPath, fileName));
        final byte[] createdData = fileStorageService.readBytes(dataPath, fileName).get();
        assertTrue(Arrays.equals(content1, createdData));

    }
    
    /**
     * Tests the no content validation
     */
    @Test
    public void noContentCreateTest() {
    	expectedException.expect(HengeValidationException.class);
    	expectedException.expectMessage("File object contains no data");
    	
    	repository.create(FileVersion.builder("Test__File1", "1.0.0", null, "test_file.txt")
                .withDescription("File 1 description").withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.of(2016, 3, 22, 10, 45))
                .build());
    }

    /**
     * Test rollback for creation
     */
    @Test
    public void creationRollbackTest() throws Exception {

        final String fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.0");
        // writes a file on the data folder to create an IO error due to file
        // already existing.
        // so, the model file will be successfully created and the data file
        // won't.
        // the API must delete the model file created because the creation of
        // the data file associated with it failed.
        fileStorageService.write(dataPath, fileName, "Lorem ipsum dolor sit amet...", true);

        expectedException.expect(RuntimeHengeException.class);
        repository.create(fileVersion);

        assertFalse(fileStorageService.exists(modelPath, fileName));

    }

    /**
     * Test the successful updating of FileVersions
     * 
     * @throws Exception
     */
    @Test
    public void updateTest() throws Exception {

        repository.create(fileVersion);

        final FileVersion fileVersion2 = FileVersion.builder(fileVersion).withVersion("1.0.1").build();

        final Optional<FileVersion> updated = repository.update("Test__File1", fileVersion2);

        assertTrue(updated.isPresent());
        assertThat("Test__File1", equalTo(updated.get().getName()));
        assertThat("Content file", equalTo(new String(updated.get().getContent(), UTF_8)));

        final String fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.1");
        assertTrue(fileStorageService.exists(modelPath, fileName));

        final FileVersion updatedModel =
            jsonUtils.fromJson(fileStorageService.read(modelPath, fileName).get(), FileVersion.class);
        assertThat(fileVersion2.getName(), equalTo(updatedModel.getName()));
        assertThat(fileVersion2.getVersion(), equalTo(updatedModel.getVersion()));
        assertThat(fileVersion2.getDescription(), equalTo(updatedModel.getDescription()));
        assertThat(fileVersion2.getCreatedBy(), equalTo(updatedModel.getCreatedBy()));
        assertThat(fileVersion2.getCreatedDate(), equalTo(updatedModel.getCreatedDate()));

        assertTrue(fileStorageService.exists(dataPath, fileName));
        final byte[] updatedData = fileStorageService.readBytes(dataPath, fileName).get();
        assertTrue(Arrays.equals(content1, updatedData));

    }

    /**
     * Test rollback for updates
     */
    @Test
    public void updateRollbackTest() throws Exception {

        FileVersion created = repository.create(fileVersion);

        final String fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.1");
        // writes the datafile to create an IO error due to file already
        // existing.
        fileStorageService.write(dataPath, fileName, "Lorem ipsum dolor sit amet...", true);

        expectedException.expect(RuntimeHengeException.class);
        repository.update("Test__File1", FileVersion.builder(created).withVersion("1.0.1").build());

        assertFalse(fileStorageService.exists(modelPath, fileName));

    }

    /**
     * Test updating nonexisting entity
     */
    @Test
    public void updateNonExistingTest() throws Exception {

        Optional<FileVersion> response =
            repository.update("Test__File1", FileVersion.builder(fileVersion).withVersion("1.0.1").build());

        assertFalse(response.isPresent());

    }
    
    /**
     * Tests the no content validation on update
     */
    @Test
    public void noContentUpdateTest() {
    	expectedException.expect(HengeValidationException.class);
    	expectedException.expectMessage("File object contains no data");
    	
    	repository.update("Test__File1", FileVersion.builder("Test__File1", "1.0.0", null, "test_file.txt")
                .withDescription("File 1 description").withCreatedBy("Wagner Y. Matsushita")
                .withCreatedDate(LocalDateTime.of(2016, 3, 22, 10, 45))
                .build());
    }

    /**
     * Test updating nonexisting entity
     */
    @Test
    public void updateWrongVersionTest() throws Exception {

        repository.create(fileVersion);

        expectedException.expect(HengeValidationException.class);
        // trying to update without changing version
        repository.update("Test__File1", fileVersion);

    }
    
    /**
     * Test deleting by name and version
     */
    @Test
    public void deleteByNameAndVersionTest() throws Exception {

        repository.create(fileVersion);

        // create some updates
        FileVersion fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.1").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.2").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.3").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);

        Optional<FileVersion> deleted = repository.delete("Test__File1", "1.0.1");

        assertThat(deleted.get().getVersion(), equalTo("1.0.1"));

        Optional<FileVersion> find = repository.read(fileVersion.getName(), "1.0.1");
        assertFalse(find.isPresent());
    }

    /**
     * Test deleting nonexisting entity
     */
    @Test
    public void deleteNonExistingTest() throws Exception {

        Optional<FileVersion> response = repository.delete("Test__File1");

        assertFalse(response.isPresent());

    }

    /**
     * Test deleting all versions
     */
    @Test
    public void deleteAllVersionsTest() throws Exception {

        repository.create(fileVersion);

        // create some updates
        FileVersion fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.1").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.2").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.3").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);

        Optional<FileVersion> deleted = repository.delete("Test__File1");

        assertThat(deleted.get().getVersion(), equalTo("1.0.3"));

        assertFalse(fileStorageService.existsBeginningWith(modelPath, "Test__File1"));
        assertFalse(fileStorageService.existsBeginningWith(dataPath, "Test__File1"));

    }

    /**
     * Test the bulk deletion rollback feature
     */
    @Ignore
    @Test
    public void deleteAllVersionsRollbackTest() throws Exception {
        FileVersion[] fileVersions = new FileVersion[10];
        
        fileVersions[0] = repository.create(fileVersion);

        // create some updates
        FileVersion fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.1").build();
        fileVersions[1] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.2").build();
        fileVersions[2] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.3").build();
        fileVersions[3] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.4").build();
        fileVersions[4] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.5").build();
        fileVersions[5] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.6").build();
        fileVersions[6] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.7").build();
        fileVersions[7] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.8").build();
        fileVersions[8] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.9").build();
        fileVersions[9] = repository.update(fileVersionUpdated.getName(), fileVersionUpdated).get();

        /* Create a thread to mess with the files and generate IOException
         * It detects when the first file in the list is deleted. 
         * At that point the delete method has already listed all the files it must delete,
         * so this thread will remove the last one, to trigger an Exception on the main thread. 
         * Then, all the files that were successfully deleted must be restored. 
         */
        Thread joker = new Thread(new Runnable() {

            public void run() {

                final String fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.0");

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(5);
                        if (!fileStorageService.exists(dataPath, fileName)) {
                            LOGGER.info("Boom!!!!");
                            fileStorageService.delete(dataPath,
                                fileNamingService.getCompleteFileName("Test__File1", "1.0.9"));
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
                LOGGER.info("Joker Thread interrupted.");

            }

        });

        joker.start();

        expectedException.expect(HengeIOException.class);

        repository.delete("Test__File1");

        // Checks that all the files were restored.
        String fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.0");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[0]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.1");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[1]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.2");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[2]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.3");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[3]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.4");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[4]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.5");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[5]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.6");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[6]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.7");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[7]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.8");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[8]));

        fileName = fileNamingService.getCompleteFileName("Test__File1", "1.0.9");
        assertTrue(fileStorageService.exists(modelPath, fileName));
        assertTrue(fileStorageService.exists(dataPath, fileName));
        assertThat(fileStorageService.read(modelPath, fileName), equalTo(fileVersions[9]));

        joker.interrupt();

    }

    /**
     * Test successful reading of a specific version of a {@link FileVersion}
     */
    @Test
    public void readSpecificVersionTest() {

        repository.create(fileVersion);

        Optional<FileVersion> readFileVersion = repository.read(fileVersion.getName(), fileVersion.getVersion());

        assertThat(readFileVersion.get(), equalTo(fileVersion));

    }

    /**
     * Test successful reading of a specific version of a {@link FileVersion}
     */
    @Test
    public void readNonExistingVersionTest() {

        Optional<FileVersion> readFileVersion = repository.read(fileVersion.getName(), fileVersion.getVersion());

        assertFalse(readFileVersion.isPresent());

    }

    /**
     * Test successful reading of a specific version of a {@link FileVersion}
     */
    @Test
    public void readLatestVersionTest() {

        repository.create(fileVersion);

        FileVersion fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.1").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.2").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);
        fileVersionUpdated = FileVersion.builder(fileVersion).withVersion("1.0.3").build();
        repository.update(fileVersionUpdated.getName(), fileVersionUpdated);

        Optional<FileVersion> readFileVersion = repository.read(fileVersion.getName());

        assertThat(readFileVersion.get(), equalTo(fileVersionUpdated));

    }

    @After
    public void tearDown() {

        final String modelPath = fileNamingService.getPath(FileVersion.class);
        final String dataPath =
            fileNamingService.getPath(FileVersion.class, FileVersionFlatFileRepository.DATA_SUB_FOLDER_NAME);
        fileStorageService.deleteBeginningWith(modelPath, "Test__");
        fileStorageService.deleteBeginningWith(dataPath, "Test__");

    }

}
