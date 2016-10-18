package com.kenzan.henge.repository.impl.cassandra;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.MappingFactory;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.repository.MappingRepository;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","cassandra","setmapping"})
public class VersionSetMappingCassandraRepositoryIT {
	
	private static final String VERSION_SET_MAPPING_COLUMNFAMILY = "version_set_mapping";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	private Session session;
	
	@Autowired
	private MappingFactory<VersionSetReference> mappingFactory;
	
	@Autowired
	private MappingRepository<VersionSetReference> versionSetMappingRepository;	
	
	private String KEY = "{\"scopeSet\":[{\"key\":\"application\",\"value\":\"AppName\"}]}";
	private String VALUE = "{\"name\":\"AppName\",\"version\":\"1.0.0\"}"; 
	
	@Before
	public void init() {
		
	    final Insert insert = QueryBuilder.insertInto(VERSION_SET_MAPPING_COLUMNFAMILY)
					.value("key", KEY)
					.value("value", VALUE);
		session.execute(insert);
	}
	
	@Test
	public void loadTest() {
		
	    final MappingKey key = new MappingKey(Sets.newSet(Scope.builder("application", "AppName").build()));
	    final VersionSetReference value = VersionSetReference.builder("AppName","1.0.0").build();
	    final Mapping<VersionSetReference> expectedMapping = mappingFactory.create();
		expectedMapping.put(key, value);
		
		final Mapping<VersionSetReference> mapping = versionSetMappingRepository.load();
		
		assertEquals(expectedMapping, mapping);
	}
	
	@Test
	public void saveTest() {
		
	    final MappingKey key = new MappingKey(Sets.newSet(Scope.builder("application", "AppName").build()));
	    final VersionSetReference value = VersionSetReference.builder("AppName","1.0.0").build();
	    final Mapping<VersionSetReference> mappingToSave = mappingFactory.create();
		mappingToSave.put(key, value);
		
		final Mapping<VersionSetReference> savedMapping = versionSetMappingRepository.save(mappingToSave);
		assertTrue(savedMapping != null);
		assertEquals(savedMapping.get(key).get(), value);
		
		final Select select = select().from(VERSION_SET_MAPPING_COLUMNFAMILY);
		select.where(eq("key", KEY));
		
		final List<Row> rows = session.execute(select).all();
		assertFalse(rows.isEmpty());
		assertTrue(rows.get(0) != null);
		assertEquals(rows.get(0).getString("key"), KEY);
		assertEquals(rows.get(0).getString("value"), VALUE);
	}
}
