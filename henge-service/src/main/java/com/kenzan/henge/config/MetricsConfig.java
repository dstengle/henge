package com.kenzan.henge.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
import metrics_influxdb.api.protocols.InfluxdbProtocols;

/**
 * This class enables metrics and registration of reporters.
 * It also enables cleanup of reporters when the Spring context finishes.  
 *
 * @author wmatsushita
 */
@Configuration
@ConfigurationProperties
@EnableMetrics(proxyTargetClass = true)
@Profile("metrics")
public class MetricsConfig extends MetricsConfigurerAdapter {

	@Value("${metrics.influx.host}")
	private String host;
	
	@Value("${metrics.influx.port}")
	private int port;
	
	@Value("${metrics.influx.user}")
	private String user;
	
	@Value("${metrics.influx.password}")
	private String password;
	
	@Value("${metrics.influx.database}")
	private String database;
	
	@Value("${metrics.influx.periodInSeconds}")
	private int periodInSeconds;
	
    @Override
    public void configureReporters(MetricRegistry metricRegistry) {
     final ScheduledReporter reporter = InfluxdbReporter.forRegistry(metricRegistry)
    		    .protocol(InfluxdbProtocols.http(host, port, user, password, database))
    		    .convertRatesTo(TimeUnit.SECONDS)
    		    .convertDurationsTo(TimeUnit.MILLISECONDS)
    		    .filter(MetricFilter.ALL)
    		    .skipIdleMetrics(false)
    		    .tag("cluster", "CL01")
    		    .tag("client", "OurImportantClient")
    		    .tag("server", "localhost")
    		    .transformer(new CategoriesMetricMeasurementTransformer("module", "artifact"))
    		    .build();
    		reporter.start(periodInSeconds, TimeUnit.SECONDS);
     
    }
    
}
