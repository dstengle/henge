package com.kenzan.henge.config;

import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.repository.MappingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Fetches the mapping from the repository and makes it available for injection 
 *
 * @author wmatsushita
 */
@Configuration
public class VersionSetMappingConfig {
    
    @Autowired
    private MappingRepository<VersionSetReference> repository;
    
    @Bean
    public Mapping<VersionSetReference> getVersionSetMapping() {
        
        return repository.load();
        
    }
    
}
