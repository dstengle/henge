package com.kenzan.henge.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.datastax.driver.core.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","cassandra","setmapping"})
public class CassandraConfigIT {
	
	@Autowired
	private Session session;
	
	@Test
	public void testConnection() {
		assertNotNull(session);
		assertFalse(session.isClosed());
	}

}
