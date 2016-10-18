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
import com.kenzan.henge.domain.model.FileVersionReference;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.repository.VersionSetRepository;

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
public class VersionSetCassandraRepositoryIT {
	
	private static final String VERSION_SET_COLUMNFAMILY = "version_set";

	private PropertyGroupReference propertyGroupReference1, propertyGroupReference2;
	
	private FileVersionReference fileVersionReference1, fileVersionReference2;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	private Session session;
	
	@Autowired
	private VersionSetRepository repository;
	
	/**
	 * Create some initial data
	 */
	public void setup() {
		propertyGroupReference1 = PropertyGroupReference.builder("pg1", "1.0.0").build();
		propertyGroupReference2 = PropertyGroupReference.builder("pg2", "2.0.4").build();
		
		fileVersionReference1 = FileVersionReference.builder("fileVersion1", "1.0.0").build();
		fileVersionReference2 = FileVersionReference.builder("fileVersion2", "1.0.1").build();
	}
	
	/**
	 * Successful creation test
	 */
	@Test
	public void createTest() {
		
	    final VersionSet versionSet = VersionSet.builder("VersionSetTest1", "1.0.0")
				.withDescription("PropertyGroup 1 Description")
				.withCreatedBy("Igor K. Shiohara")
				.withCreatedDate(LocalDateTime.now())
				.withPropertyGroupReferences(propertyGroupReference1, propertyGroupReference2)
				.withFileVersionReferences(fileVersionReference1, fileVersionReference2)
				.build();		
		
	    final VersionSet created = repository.create(versionSet);
		assertNotNull(created);
		assertEquals(versionSet, created);
	}
	
	/**
	 * Successful updating test
	 */
	@Test
	public void updateTest() {
	    
	    final Insert insert = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VersionSet-NAME")
				.value("version", "1.0.0")
				.value("description", "Pg Description")
				.value("is_type_hierarchy_enabled", true);
		session.execute(insert);
		
		final Optional<VersionSet> versionSet = 
		                repository.update("VersionSet-NAME", VersionSet.builder("VersionSet-NAME", "1.0.1")
		                    .withDescription("VersionSet Description changed")
							.build());
		assertTrue(versionSet.isPresent());
		final Optional<Row> row = select("VersionSet-NAME", "1.0.1");
		assertTrue(row.isPresent());
		assertEquals("VersionSet Description changed", row.get().getString("description"));
	}
	
	/**
	 * Trying to updating a not existing property group test
	 */
	@Test
	public void updateFailTest() {
		
	    final Optional<VersionSet> versionSet = 
	                    repository.update("UNKOWN-VERSIONSET-NAME", VersionSet.builder("VERSION-SET-NAME", "1.0.1")
	                        .withDescription("VersionSet Description changed")
							.build());
		assertEquals(Optional.empty(), versionSet);
	}
	
	/**
	 *  Successful deletion by name test
	 */
	@Test
	public void deleteTest() {
		
	    final Insert insert = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VS-NAME-TO-DELETE")
				.value("version", "1.0.0")
				.value("description", "Pg Description");
		session.execute(insert);
		
		final Optional<VersionSet> delete = repository.delete("VS-NAME-TO-DELETE");
		assertTrue(delete.isPresent());
		
		final Optional<Row> row = select("VS-NAME-TO-DELETE", "1.0.0");
		assertFalse(row.isPresent());
	}
	
	/**
	 *  Fail deletion by name test
	 */
	@Test
	public void deleteFailTest() {
		expectedException.expect(HengeResourceNotFoundException.class);
		expectedException.expectMessage("No data were found with name: UNEXISTENT-VS-NAME-TO-DELETE to delete.");
		
		repository.delete("UNEXISTENT-VS-NAME-TO-DELETE");
	}
	
	/**
	 *  Successful deletion by name and version test
	 */
	@Test
	public void deleteByNameAndVersionTest() {
		
	    final Insert insert1 = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VS-NAME-TO-DELETE")
				.value("version", "1.0.0")
				.value("description", "Vs Description");
		
	    final Insert insert2 = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VS-NAME-TO-DELETE")
				.value("version", "1.0.1")
				.value("description", "Vs Description");
		session.execute(insert1);
		session.execute(insert2);
		
		final Optional<VersionSet> delete = repository.delete("VS-NAME-TO-DELETE", "1.0.1");
		assertTrue(delete.isPresent());
		
		final Optional<Row> row1 = select("VS-NAME-TO-DELETE", "1.0.0");
		assertTrue(row1.isPresent());
		
		final Optional<Row> row2 = select("VS-NAME-TO-DELETE", "1.0.1");
		assertFalse(row2.isPresent());
	}
	
	/**
	 *  Fail deletion by name and version test
	 */
	@Test
	public void deleteByNameAndVersionFailTest() {
		expectedException.expect(HengeResourceNotFoundException.class);
		expectedException.expectMessage("No data were found with name: UNEXISTENT-VS-NAME-TO-DELETE and version 1.0.0 to delete.");
		
		repository.delete("UNEXISTENT-VS-NAME-TO-DELETE", "1.0.0");
	}
	
	/**
	 * Successful read by name test
	 */
	@Test
	public void readByNameTest() {
		
	    final Insert insert = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VS-NAME-TO-READ")
				.value("version", "1.0.0")
				.value("description", "Vs Description");
		session.execute(insert);
		
		final Optional<VersionSet> versionSet = repository.read("VS-NAME-TO-READ");
		assertTrue(versionSet.isPresent());
		assertEquals("1.0.0", versionSet.get().getVersion());
		assertEquals("Vs Description", versionSet.get().getDescription());
	}
	
	/**
	 * Successful read by name and version test
	 */
	@Test
	public void readByNameAndVersionTest() {
		
	    final Insert insert = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VS-NAME-TO-READ")
				.value("version", "1.0.0")
				.value("description", "Vs Description");
		session.execute(insert);
		
		final Optional<VersionSet> propertyGroup = repository.read("VS-NAME-TO-READ", "1.0.0");
		assertTrue(propertyGroup.isPresent());
		assertEquals("1.0.0", propertyGroup.get().getVersion());
		assertEquals("Vs Description", propertyGroup.get().getDescription());
	}
	
	/**
	 * Successful retrieve all versions test
	 */
	@Test
	public void retrieveAllVersionsTest() {
		
	    final Insert insert1 = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VS-NAME-1")
				.value("version", "1.0.0")
				.value("description", "Vs Description");
	    final Insert insert2 = QueryBuilder.insertInto(VERSION_SET_COLUMNFAMILY)
				.value("name", "VS-NAME-1")
				.value("version", "1.0.1")
				.value("description", "Vs Description");
		
		session.execute(insert1);
		session.execute(insert2);
		
		final Optional<Set<String>> versions = repository.versions("VS-NAME-1");
		assertTrue(versions.isPresent());
		assertEquals(2, versions.get().size());
		assertTrue(versions.get().contains("1.0.0"));
		assertTrue(versions.get().contains("1.0.1"));
	}
	
	/**
	 * Utility method to search on casssandra db
	 */
	private Optional<Row> select(String name, String version) {
		
	    final Select select = QueryBuilder.select().from(VERSION_SET_COLUMNFAMILY);
		select.where(eq("name", name)).and(eq("version", version));
		return session.execute(select).all().stream().findFirst();
	}

}
