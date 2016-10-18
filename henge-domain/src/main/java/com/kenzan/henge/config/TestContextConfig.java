package com.kenzan.henge.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Provides the spring context and configuration for the dependency injection and properties injection in tests.
 * This class is located here in order to provide this context to all the tests in all the project modules.
 * 
 * @author wmatsushita
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan("com.kenzan.henge")
public class TestContextConfig {
    
}