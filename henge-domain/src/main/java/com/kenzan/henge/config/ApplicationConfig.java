package com.kenzan.henge.config;

import com.kenzan.henge.domain.model.ScopePrecedenceConfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * This class contains configurations pertaining to the business logic of 
 * the Henge itself.
 *
 * @author wmatsushita
 */
@Configuration
public class ApplicationConfig {
    
    @Value("${scope.precedence.configuration}") 
    private String configString;
    
    @Bean
    public ScopePrecedenceConfiguration scopePrecedenceConfiguration() {
        
        return new ScopePrecedenceConfiguration(configString);

    }

}
