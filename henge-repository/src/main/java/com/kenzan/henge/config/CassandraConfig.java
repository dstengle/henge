package com.kenzan.henge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
* 
* This class provides a Cassandra connection
* 
* @author Igor K. Shiohara
*
*/
@Configuration
@ConfigurationProperties
@EnableAutoConfiguration
@Profile("cassandra")
public class CassandraConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraConfig.class);
	
	@Value("${cassandra.host}")
    private String host;
	
	@Value("${cassandra.port}")
    private int port;
	
	@Value("${cassandra.keyspace}")
    private String keyspace;
	
	/**
	 * Cassandra cluster
	 */
	private Cluster cluster;
	
	/**
	 * Cassandra session
	 */
	private Session session;
	
	/**
	 * Retrieves the Cassandra session
	 * @return {@link Session}
	 */
	@Bean
	public Session getSession() {
		if (session == null) {
			cluster = Cluster.builder()
					.withPort(port)
					.addContactPoint(host).build();
			session = cluster.connect(keyspace);
			LOGGER.info("Connected on "+ host + ":" + port);
		}
		return session;
	}
	
}