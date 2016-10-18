package com.kenzan.henge.repository.impl.cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.repository.CurrentFileVersionRepository;
import com.kenzan.henge.repository.FileVersionRepository;

/**
 * @author Igor K. Shiohara
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","cassandra","setmapping"})
public class CurrentFileVersionCassandraRepositoryIT {
	
	private static final String FILE_VERSION_CURRENT = "file_version_current";

	@Autowired
	private Session session;
	
	@Autowired
	private CurrentFileVersionRepository currentFileVersionRepository;
	
	@Test
	public void setCurrentSuccessTest() {
		final String name = "FileVersion1";
		final String version = "1.0.0";
		
		currentFileVersionRepository.setCurrentVersion(name, version);
		checkExistence(name, version);
	}
	
	@Test
	public void getCurrentVersionSuccessTest() {
		final String name = "FileVersionTest";
		final String version = "2.0.1";
		createSomeCurrentFileVersion(name, version);
		
		Optional<String> currentVersion = currentFileVersionRepository.getCurrentVersion(name);
		assertTrue(currentVersion.isPresent());
		assertEquals("2.0.1", currentVersion.get());
	}
	
	private void checkExistence(String name, String version) {
		Statement select = QueryBuilder.select().from(FILE_VERSION_CURRENT).where(QueryBuilder.eq("name", name));
		assertFalse(session.execute(select).all().isEmpty());
		
	}

	private void createSomeCurrentFileVersion(final String name, final String version) {
		Insert insertCurrentVersion = QueryBuilder.insertInto(FILE_VERSION_CURRENT)
				.value("name", name)
				.value("version", version);
		session.execute(insertCurrentVersion);
	}

}
