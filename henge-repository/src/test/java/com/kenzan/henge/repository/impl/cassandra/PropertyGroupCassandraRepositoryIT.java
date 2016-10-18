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
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.repository.PropertyGroupRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
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
public class PropertyGroupCassandraRepositoryIT {
	
	private static final String PROPERTY_GROUP_COLUMNFAMILY = "property_group";

	private Property property1, property2;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	private Session session;
	
	@Autowired
	private PropertyGroupRepository repository;
	
	/**
	 * Create some initial data
	 */
	@Before
	public void setup() {
	    
		property1 = new Property.Builder("app-name")
				.withDescription("Application Description")
				.withScopedValues(
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Pet Store App - Development").build(),
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Pet Store App - Production").build()							
				)
				.withDefaultValue("Default value")
				.build();
		property2 = new Property.Builder("property-foo")
				.withDescription("Dummy Description")
				.withScopedValues(
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=dev"), "Bar Dev").build(), 
						PropertyScopedValue.builder(ScopeUtils.parseScopeString("env=prod"), "Bar Prod").build()
				)
				.withDefaultValue("Default value 2")
				.build();
	}
	
	/**
	 * Successful creation test
	 */
	@Test
	public void createTest() {
	    
		final PropertyGroup propertyGroup = PropertyGroup.builder("PropertyGroupTest1", "1.0.0")
				.withDescription("PropertyGroup 1 Description")
				.withType(PropertyGroupType.APP.name())
				.withIsActive(true)
				.withCreatedBy("Igor K. Shiohara")
				.withCreatedDate(LocalDateTime.now())
				.withProperties(property1, property2)
				.build();		
		
		final PropertyGroup created = repository.create(propertyGroup);
		assertNotNull(created);
		assertEquals(propertyGroup, created);
	}
	
	/**
	 * Successful updating test
	 */
	@Test
	public void updateTest() {
		
	    final Insert insert = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME")
				.value("version", "1.0.0")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
		session.execute(insert);
		
		final Optional<PropertyGroup> propertyGroup = repository.update("PG-NAME", PropertyGroup.builder("PG-NAME", "1.0.1")
										.withDescription("Pg Description changed")
										.withType("APP")
										.withIsActive(true)
									 .build());
		assertTrue(propertyGroup.isPresent());
		final Optional<Row> row = select("PG-NAME", "1.0.1");
		assertTrue(row.isPresent());
		assertEquals("Pg Description changed", row.get().getString("description"));
	}
	
	/**
	 * Trying to updating a not existing property group test
	 */
	@Test
	public void updateFailTest() {
	    
	    final Optional<PropertyGroup> propertyGroup = repository.update("UNKOWN-PG-NAME", PropertyGroup.builder("PG-NAME", "1.0.1")
										.withDescription("Pg Description changed")
										.withType("APP")
										.withIsActive(true)
									 .build());
		assertEquals(Optional.empty(), propertyGroup);
	}
	
	/**
	 *  Successful deletion by name test
	 */
	@Test
	public void deleteTest() {
		
	    final Insert insert = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME-TO-DELETE")
				.value("version", "1.0.0")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
		session.execute(insert);
		
		final Optional<PropertyGroup> delete = repository.delete("PG-NAME-TO-DELETE");
		assertTrue(delete.isPresent());
		
		final Optional<Row> row = select("PG-NAME-TO-DELETE", "1.0.0");
		assertFalse(row.isPresent());
	}
	
	/**
	 *  Fail deletion by name test
	 */
	@Test
	public void deleteFailTest() {
		
	    expectedException.expect(HengeResourceNotFoundException.class);
		expectedException.expectMessage("No data were found with name: UNEXISTENT-PG-NAME-TO-DELETE to delete.");
		
		repository.delete("UNEXISTENT-PG-NAME-TO-DELETE");
	}
	
	/**
	 *  Successful deletion by name and version test
	 */
	@Test
	public void deleteByNameAndVersionTest() {
		
	    final Insert insert1 = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME-TO-DELETE")
				.value("version", "1.0.0")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
		
	    final Insert insert2 = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME-TO-DELETE")
				.value("version", "1.0.1")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
		session.execute(insert1);
		session.execute(insert2);
		
		final Optional<PropertyGroup> delete = repository.delete("PG-NAME-TO-DELETE", "1.0.1");
		assertTrue(delete.isPresent());
		
		final Optional<Row> row1 = select("PG-NAME-TO-DELETE", "1.0.0");
		assertTrue(row1.isPresent());
		
		final Optional<Row> row2 = select("PG-NAME-TO-DELETE", "1.0.1");
		assertFalse(row2.isPresent());
	}
	
	/**
	 *  Fail deletion by name and version test
	 */
	@Test
	public void deleteByNameAndVersionFailTest() {
		
	    expectedException.expect(HengeResourceNotFoundException.class);
		expectedException.expectMessage("No data were found with name: UNEXISTENT-PG-NAME-TO-DELETE and version 1.0.0 to delete.");
		
		repository.delete("UNEXISTENT-PG-NAME-TO-DELETE", "1.0.0");
	}
	
	/**
	 * Successful read by name test
	 */
	@Test
	public void readByNameTest() {
		
	    final Insert insert = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME-TO-READ")
				.value("version", "1.0.0")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
		session.execute(insert);
		
		final Optional<PropertyGroup> propertyGroup = repository.read("PG-NAME-TO-READ");
		assertTrue(propertyGroup.isPresent());
		assertEquals("1.0.0", propertyGroup.get().getVersion());
		assertEquals("Pg Description", propertyGroup.get().getDescription());
		assertEquals("APP", propertyGroup.get().getType());
		assertTrue(propertyGroup.get().isActive());
	}
	
	/**
	 * Successful read by name and version test
	 */
	@Test
	public void readByNameAndVersionTest() {
		
	    final Insert insert = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME-TO-READ")
				.value("version", "1.0.0")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
		session.execute(insert);
		
		final Optional<PropertyGroup> propertyGroup = repository.read("PG-NAME-TO-READ", "1.0.0");
		assertTrue(propertyGroup.isPresent());
		assertEquals("1.0.0", propertyGroup.get().getVersion());
		assertEquals("Pg Description", propertyGroup.get().getDescription());
		assertEquals("APP", propertyGroup.get().getType());
		assertTrue(propertyGroup.get().isActive());
	}
	
	/**
	 * Successful retrieve all versions test
	 */
	@Test
	public void retrieveAllVersionsTest() {
	    
	    final Insert insert1 = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME-1")
				.value("version", "1.0.0")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
	    
	    final Insert insert2 = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMNFAMILY)
				.value("name", "PG-NAME-1")
				.value("version", "1.0.1")
				.value("description", "Pg Description")
				.value("type", "APP")
				.value("is_active", true);
		
		session.execute(insert1);
		session.execute(insert2);
		
		final Optional<Set<String>> versions = repository.versions("PG-NAME-1");
		assertTrue(versions.isPresent());
		assertEquals(2, versions.get().size());
		assertTrue(versions.get().contains("1.0.0"));
		assertTrue(versions.get().contains("1.0.1"));
	}
	
	/**
	 * Utility method to search on casssandra db
	 */
	private Optional<Row> select(String name, String version) {
		
	    final Select select = QueryBuilder.select().from(PROPERTY_GROUP_COLUMNFAMILY);
		select.where(eq("name", name)).and(eq("version", version));
		return session.execute(select).all().stream().findFirst();
	}

}
