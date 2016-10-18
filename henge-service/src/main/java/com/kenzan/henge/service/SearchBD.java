package com.kenzan.henge.service;

import java.util.Optional;

import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;

/**
 * Provides the search service
 *
 * @author wmatsushita
 */
public interface SearchBD {

    /**
     * Finds the {@link VersionSet} mapped to the given search parameters and
     * process it, returning all the {@link Property} evaluated by the given
     * scope in text format.
     * 
     * @param application the name of the application
     * @param scopeString the set of scopes in string format
     * @param libs the list of libraries desired as part of the properties
     *        returned. If present, any {@link PropertyGroup} of type library whose name is
     *        not present in the list will be discarded.
     * @return all the {@link Property} evaluated by the given scope in text
     *         format.
     */
    public Optional<String> findProperties(final String application, final String scopeString, final Optional<String> libs);
    
}
