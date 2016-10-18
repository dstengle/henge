package com.kenzan.henge.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;

/**
 * This class provides methods for finding matching {@link VersionSet} given the
 * search parameters.
 *
 * @author wmatsushita
 */
public interface VersionSetMappingBD {

    /**
     * Returns an {@link Optional} of a {@link VersionSet}, given the
     * application name and version.
     * 
     * @param application name of the application
     * @param scopeSet set of {@link Scope}s
     * @return an {@link Optional} of a {@link VersionSet} mapped the the given
     *         application name and version.
     */
    public Optional<VersionSet> findMatch(String application, Set<Scope> scopeSet);
    
    /**
     * Adds a new entry into the mapping
     * 
     * @param application the name of the application. It is optional
     * @param scopeSet set of {@link Scope}s
     * @param versionSetReference the {@link VersionSetReference} to be mapped.
     */
    public void setMapping(Optional<String> application, Set<Scope> scopeSet, VersionSetReference versionSetReference);
    
    /**
     * Retrieves all the currently existing mapping entries
     * 
     * @return a Map where the key is the {@link MappingKey} and the value is the {@link VersionSetReference}.
     */
    public Map<MappingKey, VersionSetReference> getAllMappings();
    
    
    /**
     * Removes the given entry from the Mappings.
     * 
     * @param application the name of the application. It is optional
     * @param scopeSet set of {@link Scope}s
     */
    public void deleteMapping(Optional<String> application, Set<Scope> scopeSet);

}
