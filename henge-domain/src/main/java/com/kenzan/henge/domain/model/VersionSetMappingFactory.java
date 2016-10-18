package com.kenzan.henge.domain.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for {@link VersionSetMapping}. This provides polymorphism for the
 * {@link Mapping} interface in a non Spring singleton way.
 *
 * @author wmatsushita
 */
@Component
public class VersionSetMappingFactory implements MappingFactory<VersionSetReference> {

    private ScopePrecedenceConfiguration scopePrecedenceConfiguration;
    
    @Autowired
    public VersionSetMappingFactory(ScopePrecedenceConfiguration scopePrecedenceConfiguration) {
        this.scopePrecedenceConfiguration = scopePrecedenceConfiguration;
    }

    public Mapping<VersionSetReference> create() {

        return new VersionSetMapping(scopePrecedenceConfiguration);

    }

}
