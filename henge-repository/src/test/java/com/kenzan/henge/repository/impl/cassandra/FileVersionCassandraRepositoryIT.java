package com.kenzan.henge.repository.impl.cassandra;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.repository.FileVersionRepository;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","cassandra","setmapping"})
public class FileVersionCassandraRepositoryIT {
	
	private static final String FILE_VERSION_COLUMN_FAMILY = "file_version";

	private byte[] content1 = "Content file".getBytes();
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	private Session session;
	
	@Autowired
	private FileVersionRepository repository;
	
	/**
	 * Successful creation test
	 */
	@Test
	public void createTest() {
		FileVersion fileVersion = FileVersion.builder("File1", "1.0.0", content1, "test_file")
									.withDescription("File 1 description")
									.withCreatedBy("Igor K. Shiohara")
									.withCreatedDate(LocalDateTime.of(2016, 3, 22, 10, 45))
								.build();
		FileVersion created = repository.create(fileVersion);
		assertNotNull(created);
		assertEquals("File1", created.getName());
	}
	
	/**
	 * Successful updating test
	 */
	@Test
	public void updateTest() {
		Insert insert = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FileName1")
				.value("version", "1.0.0")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("Initial content".getBytes()));
		session.execute(insert);
		
		Optional<FileVersion> updated = repository.update(
		    "FileName1", 
		    FileVersion.builder("FileName1", "1.0.1", "Updated content".getBytes(), "test_file")
						.withDescription("File 1 description")
						.build());
		assertTrue(updated.isPresent());
		assertEquals("FileName1", updated.get().getName());
		assertEquals("Updated content", new String(updated.get().getContent(), StandardCharsets.UTF_8));
		assertEquals("1.0.1", updated.get().getVersion());
	}	
	
	/**
	 * Trying to updating a not existing file test
	 */
	@Test
	public void updateFailTest() {
		Optional<FileVersion> fileVersion = repository.update("UnknownFileName", 
		    FileVersion.builder("UnknownFileName", "1.0.1", "Updated content".getBytes(), "test_file")
				.withDescription("File 1 description")
				.build());
		assertEquals(Optional.empty(), fileVersion);
	}
	
	/**
	 *  Successful deletion by name test
	 */
	@Test
	public void deleteTest() {
		Insert insert = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FILE-TO-DELETE")
				.value("version", "1.0.0")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("File content".getBytes()));
		session.execute(insert);
		
		Optional<FileVersion> delete = repository.delete("FILE-TO-DELETE");
		assertTrue(delete.isPresent());
		
		Optional<Row> row = select("FILE-TO-DELETE", "1.0.0");
		assertFalse(row.isPresent());
	}
	
	/**
	 *  Fail deletion by name test
	 */
	@Test
	public void deleteFailTest() {
		expectedException.expect(HengeResourceNotFoundException.class);
		expectedException.expectMessage("No data were found with name: UNEXISTENT-FILE-TO-DELETE to delete.");
		
		repository.delete("UNEXISTENT-FILE-TO-DELETE");
	}
	
	/**
	 *  Successful deletion by name and version test
	 */
	@Test
	public void deleteByNameAndVersionTest() {
		Insert insert1 = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FILE-TO-DELETE")
				.value("version", "1.0.0")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("File content".getBytes()));
		
		Insert insert2 = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FILE-TO-DELETE")
				.value("version", "1.0.1")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("File content".getBytes()));
		session.execute(insert1);
		session.execute(insert2);
		
		Optional<FileVersion> delete = repository.delete("FILE-TO-DELETE", "1.0.1");
		assertTrue(delete.isPresent());
		
		java.util.Optional<Row> row1 = select("FILE-TO-DELETE", "1.0.0");
		assertTrue(row1.isPresent());
		
		java.util.Optional<Row> row2 = select("FILE-TO-DELETE", "1.0.1");
		assertFalse(row2.isPresent());
	}
	
	/**
	 *  Fail deletion by name and version test
	 */
	@Test
	public void deleteByNameAndVersionFailTest() {
		expectedException.expect(HengeResourceNotFoundException.class);
		expectedException.expectMessage("No data were found with name: UNEXISTENT-FILE-TO-DELETE and version 1.0.0 to delete.");
		
		repository.delete("UNEXISTENT-FILE-TO-DELETE", "1.0.0");
	}
	
	/**
	 * Successful read by name test
	 */
	@Test
	public void readByNameTest() {
		Insert insert = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FILE-TO-READ")
				.value("version", "1.0.0")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("File content".getBytes()));
		session.execute(insert);
		
		Optional<FileVersion> fileVersion = repository.read("FILE-TO-READ");
		assertTrue(fileVersion.isPresent());
		assertEquals("1.0.0", fileVersion.get().getVersion());
		assertEquals("File Description", fileVersion.get().getDescription());
		assertEquals("File content", new String(fileVersion.get().getContent(), StandardCharsets.UTF_8));
	}
	
	/**
	 * Successful read by name and version test
	 */
	@Test
	public void readByNameAndVersionTest() {
		Insert insert = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FILE-TO-READ")
				.value("version", "1.0.0")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("File content".getBytes()));
		session.execute(insert);
		
		Optional<FileVersion> fileVersion = repository.read("FILE-TO-READ", "1.0.0");
		assertTrue(fileVersion.isPresent());
		assertEquals("1.0.0", fileVersion.get().getVersion());
		assertEquals("File Description", fileVersion.get().getDescription());
		assertEquals("File content", new String(fileVersion.get().getContent(), StandardCharsets.UTF_8));
	}
	
	/**
	 * Successful retrieve all versions test
	 */
	@Test
	public void retrieveAllVersionsTest() {
		Insert insert1 = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FILE-1")
				.value("version", "1.0.0")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("File content".getBytes()));
		Insert insert2 = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
				.value("name", "FILE-1")
				.value("version", "1.0.1")
				.value("description", "File Description")
				.value("content", ByteBuffer.wrap("File content".getBytes()));
		
		session.execute(insert1);
		session.execute(insert2);
		
		Optional<Set<String>> versions = repository.versions("FILE-1");
		assertTrue(versions.isPresent());
		assertEquals(2, versions.get().size());
		assertTrue(versions.get().contains("1.0.0"));
		assertTrue(versions.get().contains("1.0.1"));
	}
	
	/**
	 * Utility method to search on casssandra db
	 */
	private java.util.Optional<Row> select(String name, String version) {
		Select select = QueryBuilder.select().from(FILE_VERSION_COLUMN_FAMILY);
		select.where(eq("name", name)).and(eq("version", version));
		return session.execute(select).all().stream().findFirst();
	}

}
