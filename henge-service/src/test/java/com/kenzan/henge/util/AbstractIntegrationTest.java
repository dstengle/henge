package com.kenzan.henge.util;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.RestAssured;
import com.kenzan.henge.config.TestContextConfig;

/**
 * @author Igor K. Shiohara
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@WebAppConfiguration
@IntegrationTest("server.port = 0")
@ActiveProfiles({"dev", "flatfile_local", "setmapping"})
public abstract class AbstractIntegrationTest {
	
	@Value("${local.server.port}")
	private int port;
	
	@Before
	public void setUp() {
		RestAssured.port = port;
	}	
	
}
